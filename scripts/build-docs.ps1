# ==============================================================================
# Script di generazione e aggiornamento documentazione di progetto
# - Generazione Javadoc aggregato con visibilità privata in docs/javadoc/
# - Rendering PDF ad alta definizione in docs/pdf/ per:
#     * docs/guida-tecnica.md             -> docs/pdf/guida-tecnica.pdf
#     * docs/guida-utente-installazione.md -> docs/pdf/guida-utente-installazione.pdf
#     * README.md                         -> docs/pdf/README.pdf
# ==============================================================================

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

# Radice del progetto (la directory padre di scripts/)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$repoRoot = (Resolve-Path "$scriptDir\..").Path
Set-Location $repoRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Avvio generazione documentazione - Albero di Regressione" -ForegroundColor Cyan
Write-Host "  Repository: $repoRoot" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Creazione cartelle docs/javadoc e docs/pdf se non presenti
$docsPdfDir = Join-Path $repoRoot "docs\pdf"
$docsJavadocDir = Join-Path $repoRoot "docs\javadoc"

if (-not (Test-Path $docsPdfDir)) {
    New-Item -ItemType Directory -Path $docsPdfDir -Force | Out-Null
}
if (-not (Test-Path $docsJavadocDir)) {
    New-Item -ItemType Directory -Path $docsJavadocDir -Force | Out-Null
}

# 2. Generazione Javadoc aggregato tramite Maven
Write-Host "`n[1/3] Generazione Javadoc aggregato con Maven..." -ForegroundColor Yellow
mvn javadoc:aggregate -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Errore durante l'esecuzione di 'mvn javadoc:aggregate'"
}

# Sincronizza i file da apidocs alla radice di docs/javadoc se necessario
$apidocsPath = Join-Path $docsJavadocDir "apidocs"
if (Test-Path $apidocsPath) {
    Copy-Item -Path "$apidocsPath\*" -Destination $docsJavadocDir -Recurse -Force
}
Write-Host "  -> Javadoc generato con successo in $docsJavadocDir" -ForegroundColor Green

# 3. Individuazione browser Chromium per il rendering headless (Chrome o Edge)
$browserExe = $null
$chromePath = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$edgePath = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"

if (Test-Path $chromePath) {
    $browserExe = $chromePath
} elseif (Test-Path $edgePath) {
    $browserExe = $edgePath
} else {
    Write-Error "Nessun browser Chromium trovato (Chrome o Edge richiesti per il rendering PDF)."
}
Write-Host "`n[2/3] Individuato browser per rendering PDF: $browserExe" -ForegroundColor Yellow

# Foglio di stile CSS per il rendering pulito ed elegante dei documenti
$tempCss = Join-Path $env:TEMP "map_docs_style.css"
@'
body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    line-height: 1.6;
    color: #24292e;
    max-width: 900px;
    margin: 0 auto;
    padding: 25px;
}
h1, h2, h3, h4 {
    color: #1a202c;
    border-bottom: 1px solid #eaecef;
    padding-bottom: 0.3em;
    margin-top: 1.5em;
}
h1 { font-size: 2em; border-bottom: 2px solid #0366d6; }
h2 { font-size: 1.5em; }
h3 { font-size: 1.25em; border-bottom: none; }
code {
    background-color: #f6f8fa;
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
    font-size: 85%;
}
pre {
    background-color: #f6f8fa;
    border: 1px solid #e1e4e8;
    border-radius: 6px;
    padding: 14px;
    overflow: auto;
    font-size: 85%;
    line-height: 1.45;
}
pre code {
    background-color: transparent;
    padding: 0;
}
table {
    border-collapse: collapse;
    width: 100%;
    margin-top: 1em;
    margin-bottom: 1em;
}
table th, table td {
    border: 1px solid #dfe2e5;
    padding: 8px 12px;
}
table th {
    background-color: #f2f4f8;
    font-weight: 600;
}
table tr:nth-child(2n) {
    background-color: #f8fafc;
}
blockquote {
    border-left: 4px solid #0366d6;
    padding: 0.5em 1em;
    color: #4a5568;
    background-color: #f0f7ff;
    margin: 1em 0;
}
@media print {
    body {
        max-width: 100%;
        padding: 0;
    }
    h1, h2 {
        page-break-after: avoid;
    }
    pre, table {
        page-break-inside: avoid;
    }
}
'@ | Set-Content -Path $tempCss -Encoding utf8

# Funzione per convertire Markdown in PDF passando per HTML temporaneo
function Convert-MarkdownToPdf {
    param (
        [string]$SourceMd,
        [string]$TargetPdf,
        [string]$Title
    )

    $fileName = [System.IO.Path]::GetFileNameWithoutExtension($SourceMd)
    $tempHtml = Join-Path $env:TEMP "$fileName.tmp.html"
    $tempPdf = Join-Path $env:TEMP "$fileName.tmp.pdf"

    Write-Host "  -> Rendering: $SourceMd -> $TargetPdf" -ForegroundColor Cyan

    # Conversione con Pandoc in HTML autonomo con CSS
    pandoc -s "$SourceMd" -o "$tempHtml" --css="$tempCss" --metadata title="$Title"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Errore Pandoc durante la conversione di $SourceMd"
    }

    $fileUrl = "file:///" + ($tempHtml -replace '\\', '/')

    # Stampa in PDF vettoriale tramite browser Chromium headless
    Remove-Item $tempPdf -ErrorAction SilentlyContinue
    Start-Process -FilePath $browserExe -ArgumentList @(
        "--headless",
        "--disable-gpu",
        "--no-pdf-header-footer",
        "--print-to-pdf=$tempPdf",
        $fileUrl
    ) -Wait

    if (-not (Test-Path $tempPdf)) {
        Write-Error "Fallita la generazione del PDF per $SourceMd"
    }

    Copy-Item -Path $tempPdf -Destination $TargetPdf -Force
    Remove-Item $tempHtml -ErrorAction SilentlyContinue
    Remove-Item $tempPdf -ErrorAction SilentlyContinue

    $pdfSize = (Get-Item $TargetPdf).Length
    Write-Host "     [OK] PDF generato ($([Math]::Round($pdfSize / 1KB, 1)) KB)" -ForegroundColor Green
}

# 4. Generazione dei 3 file PDF richiesti
Write-Host "`n[3/3] Generazione file PDF ad alta risoluzione in docs/pdf/..." -ForegroundColor Yellow

Convert-MarkdownToPdf `
    -SourceMd (Join-Path $repoRoot "docs\guida-tecnica.md") `
    -TargetPdf (Join-Path $docsPdfDir "guida-tecnica.pdf") `
    -Title "Guida Tecnica Completa - Albero di Regressione"

Convert-MarkdownToPdf `
    -SourceMd (Join-Path $repoRoot "docs\guida-utente-installazione.md") `
    -TargetPdf (Join-Path $docsPdfDir "guida-utente-installazione.pdf") `
    -Title "Guida Utente e di Installazione - Albero di Regressione"

Convert-MarkdownToPdf `
    -SourceMd (Join-Path $repoRoot "README.md") `
    -TargetPdf (Join-Path $docsPdfDir "README.pdf") `
    -Title "README - Albero di Regressione"

# Pulizia CSS temporaneo
Remove-Item $tempCss -ErrorAction SilentlyContinue

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  Operazione completata con successo!" -ForegroundColor Green
Write-Host "  - Javadoc: $docsJavadocDir\index.html" -ForegroundColor Green
Write-Host "  - PDF:     $docsPdfDir" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
