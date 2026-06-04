# Carrega variaveis do .env para o ambiente do processo, depois sobe o Quarkus
# Necessario porque System.getenv() so le variaveis reais do SO,
# nao as carregadas pelo Quarkus via MicroProfile Config.

if (-Not (Test-Path ".env")) {
    Write-Host "Erro: .env nao encontrado na raiz do projeto." -ForegroundColor Red
    exit 1
}

Get-Content ".env" | ForEach-Object {
    $linha = $_.Trim()
    if ($linha -and -not $linha.StartsWith("#")) {
        $partes = $linha -split "=", 2
        if ($partes.Count -eq 2) {
            $nome = $partes[0].Trim()
            $valor = $partes[1].Trim().Trim('"').Trim("'")
            [Environment]::SetEnvironmentVariable($nome, $valor, "Process")
            Write-Host "Carregado: $nome" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "Subindo Quarkus em modo dev..." -ForegroundColor Cyan
.\mvnw.cmd quarkus:dev
