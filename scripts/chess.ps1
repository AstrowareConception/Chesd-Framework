param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ChessArgs
)

$ErrorActionPreference = "Stop"

$rootDir = Split-Path -Parent $PSScriptRoot
$targetDir = Join-Path $rootDir "chess-tournament/target"

$jar = Get-ChildItem -Path $targetDir -Filter "chess-tournament-*-runner.jar" -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime |
    Select-Object -Last 1

if ($null -eq $jar -or $jar.Length -le 0) {
    throw "Runner introuvable. Exécutez d'abord : mvn verify"
}

& java -jar $jar.FullName @ChessArgs

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
