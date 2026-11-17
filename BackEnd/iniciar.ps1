$ErrorActionPreference = 'Stop'

# Usa o JDK exigido pelo pom.xml tambem quando JAVA_HOME aponta para outra versao.
[xml]$pom = Get-Content (Join-Path $PSScriptRoot 'pom.xml') -Raw
$versaoJava = [string]$pom.project.properties.'java.version'
$javaNoPath = Get-Command java.exe -ErrorAction SilentlyContinue
$candidatos = @($env:JAVA_HOME)
if ($javaNoPath) {
    $candidatos += Split-Path (Split-Path $javaNoPath.Source -Parent) -Parent
}
$candidatos += Get-ChildItem "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Java" -Directory -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty FullName
$jdk = $candidatos | Where-Object {
    $_ -and (Test-Path (Join-Path $_ 'bin\javac.exe')) -and
    (Test-Path (Join-Path $_ 'release')) -and
    ([IO.File]::ReadAllText((Join-Path $_ 'release')) -match ('(?m)^JAVA_VERSION="' + [regex]::Escape($versaoJava) + '(?:\.|"|-)'))
} | Select-Object -First 1
if (-not $jdk) {
    throw "JDK $versaoJava nao encontrado. Instale essa versao ou configure JAVA_HOME para ela."
}
$env:JAVA_HOME = $jdk
$env:Path = (Join-Path $jdk 'bin') + ';' + $env:Path
Write-Host "Iniciando com JDK $versaoJava em $jdk"
# Reutiliza a chave local entre reinicializacoes, sem grava-la no Git.
if (-not $env:JWT_SECRET_BASE64) {
    $arquivoChave = Join-Path $PSScriptRoot '.jwt-secret.local'
    if (-not (Test-Path -LiteralPath $arquivoChave)) {
        $bytesChave = New-Object byte[] 32
        $geradorChave = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try { $geradorChave.GetBytes($bytesChave) }
        finally { $geradorChave.Dispose() }
        [System.IO.File]::WriteAllText($arquivoChave, [Convert]::ToBase64String($bytesChave))
    }
    $env:JWT_SECRET_BASE64 = [System.IO.File]::ReadAllText($arquivoChave).Trim()
}

Push-Location $PSScriptRoot
try {
    & mvn.cmd spring-boot:run
    $codigoMaven = $LASTEXITCODE
}
finally { Pop-Location }
exit $codigoMaven
