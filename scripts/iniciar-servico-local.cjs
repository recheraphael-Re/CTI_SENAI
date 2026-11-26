const { spawn } = require('node:child_process')
const { once } = require('node:events')
const { readFileSync } = require('node:fs')
const { createServer } = require('node:net')
const { join } = require('node:path')
const { parseEnv } = require('node:util')

const raiz = join(__dirname, '..')
const windows = process.platform === 'win32'
const limiteBuffer = 16 * 1024 * 1024
const portaEtl = 8000

function lerConfiguracao(pasta) {
  try {
    return { ...parseEnv(readFileSync(join(pasta, '.env'), 'utf8')), ...process.env }
  } catch (erro) {
    if (erro.code !== 'ENOENT') throw erro
    return process.env
  }
}

async function etlJaIniciado() {
  try {
    const resposta = await fetch(`http://127.0.0.1:${portaEtl}/openapi.json`, {
      redirect: 'error', signal: AbortSignal.timeout(1500),
    })
    if (!resposta.ok) return false
    const contrato = await resposta.json()
    return contrato.info?.title === 'CTI Insights — processamento de lotes' && Boolean(contrato.paths?.['/etl']?.post)
  } catch {
    return false
  }
}

function criarPonte(host) {
  return createServer((postgres) => {
    postgres.pause()
    const neon = new WebSocket(`wss://${host}/v2`)
    neon.binaryType = 'arraybuffer'
    const prazo = setTimeout(() => postgres.destroy(), 10_000)

    neon.addEventListener('open', () => {
      clearTimeout(prazo)
      postgres.resume()
    })
    postgres.on('data', (dados) => {
      if (neon.readyState !== WebSocket.OPEN || neon.bufferedAmount + dados.length > limiteBuffer) {
        postgres.destroy()
        return
      }
      neon.send(dados)
    })
    neon.addEventListener('message', ({ data }) => {
      if (postgres.destroyed) return
      const dados = Buffer.from(data)
      if (postgres.writableLength + dados.length > limiteBuffer) {
        postgres.destroy()
        return
      }
      postgres.write(dados)
    })
    neon.addEventListener('error', () => postgres.destroy())
    neon.addEventListener('close', () => postgres.end())
    postgres.on('error', () => postgres.destroy())
    postgres.on('close', () => {
      clearTimeout(prazo)
      if (neon.readyState < WebSocket.CLOSING) neon.close()
    })
  })
}

async function iniciar(tipo) {
  if (!['api', 'etl', 'verificar-etl'].includes(tipo)) {
    console.error('Informe api, etl ou verificar-etl.')
    return 1
  }
  if (tipo === 'etl' && await etlJaIniciado()) {
    console.log(`O ETL do CTI já está rodando em http://127.0.0.1:${portaEtl}.`)
    return 0
  }
  const api = tipo === 'api'
  const pasta = join(raiz, api ? 'BackEnd' : 'analytics-python')
  const config = lerConfiguracao(pasta)
  const endereco = api && config.DB_URL?.startsWith('jdbc:postgresql://') ? config.DB_URL.slice(5) : ''
  const url = URL.canParse(endereco) ? new URL(endereco) : null
  const host = api ? url?.hostname : config.DB_HOST

  const python = join(pasta, '.venv', windows ? 'Scripts/python.exe' : 'bin/python')
  const executar = (programa, args, env) => spawn(programa, args, { cwd: pasta, env, stdio: 'inherit' })
  if (api) {
    const compilacao = windows
      ? executar('cmd.exe', ['/d', '/c', 'mvn -DskipTests package'])
      : executar('mvn', ['-DskipTests', 'package'])
    const [codigo] = await once(compilacao, 'exit')
    if (codigo !== 0) return codigo ?? 1
  }

  // O tráfego externo usa TLS. O trecho sem TLS existe apenas no loopback.
  const env = { ...process.env }
  if (/^(?:[a-z0-9-]+\.)+neon\.tech$/i.test(host) && config.CTI_DB_TRANSPORT !== 'direct') {
    const ponte = criarPonte(host)
    ponte.listen(0, '127.0.0.1')
    await once(ponte, 'listening')
    const { port } = ponte.address()
    ponte.unref()
    if (api) {
      url.hostname = '127.0.0.1'
      url.port = String(port)
      url.searchParams.set('sslmode', 'disable')
      url.searchParams.set('channelBinding', 'disable')
      env.DB_URL = `jdbc:${url}`
      env.SERVER_ADDRESS = '127.0.0.1'
    } else {
      Object.assign(env, {
        DB_HOST: '127.0.0.1', DB_PORT: String(port), DB_SSLMODE: 'disable',
        PGGSSENCMODE: 'disable', PGCHANNELBINDING: 'disable',
      })
    }
    console.log('PostgreSQL do Neon via WebSocket seguro.')
  }

  const servico = api
    ? executar('java', ['-jar', 'target/cti-provedor-api.jar'], env)
    : executar(python, tipo === 'etl' ? ['-m', 'uvicorn', 'api:app', '--port', String(portaEtl)] : ['testar_conexao.py'], env)
  const encerrar = () => servico.kill()
  process.once('SIGINT', encerrar)
  process.once('SIGTERM', encerrar)
  const [codigo] = await once(servico, 'exit')
  return codigo ?? 1
}

iniciar(process.argv[2]).then((codigo) => process.exit(codigo)).catch((erro) => {
  console.error(`Falha ao iniciar o serviço local (${erro.code ?? erro.name}). Confira o executável e o .env do serviço.`)
  process.exit(1)
})
