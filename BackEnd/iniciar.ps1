$ErrorActionPreference = 'Stop'

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
