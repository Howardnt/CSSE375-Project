param(
  # If set, do not download ASM jars (assumes they already exist in lib/).
  [switch]$SkipDownload
)

$ErrorActionPreference = "Stop"

function Ensure-CommandExists([string]$name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
    throw "Required command not found: $name. Install a JDK and ensure it is on PATH."
  }
}

function Ensure-AsmJars([string]$libDir) {
  if ($SkipDownload) { return }

  $ver = "9.9"
  $base = "https://repo1.maven.org/maven2/org/ow2/asm"

  $jars = @(
    @{ name = "asm-$ver.jar";        url = "$base/asm/$ver/asm-$ver.jar" },
    @{ name = "asm-tree-$ver.jar";   url = "$base/asm-tree/$ver/asm-tree-$ver.jar" },
    @{ name = "asm-analysis-$ver.jar"; url = "$base/asm-analysis/$ver/asm-analysis-$ver.jar" }
  )

  foreach ($j in $jars) {
    $path = Join-Path $libDir $j.name
    if (Test-Path $path) { continue }
    Write-Host "Downloading $($j.name) ..."
    Invoke-WebRequest -Uri $j.url -OutFile $path
  }
}

function Get-JavaSources([string]$dir) {
  if (-not (Test-Path $dir)) { return @() }
  return (Get-ChildItem -Recurse $dir -Filter *.java | ForEach-Object FullName)
}

$repoRoot = $PSScriptRoot
Set-Location $repoRoot

Ensure-CommandExists "javac"
Ensure-CommandExists "java"

$libDir = Join-Path $repoRoot "lib"
$outDir = Join-Path $repoRoot "out"

New-Item -ItemType Directory -Force -Path $libDir | Out-Null
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

# Clean compilation output so removed/renamed classes don't linger in out/
Get-ChildItem -LiteralPath $outDir -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue

Ensure-AsmJars $libDir

$cp = "lib/*"

$mainSources = Get-JavaSources (Join-Path $repoRoot "src/main/java")
$testSources = Get-JavaSources (Join-Path $repoRoot "src/test/java")
$allSources = @($mainSources + $testSources)

if ($allSources.Count -eq 0) {
  throw "No Java source files found under src/main/java or src/test/java."
}

Write-Host "Compiling $($mainSources.Count) main + $($testSources.Count) test sources..."
javac -cp $cp -d $outDir @allSources
if ($LASTEXITCODE -ne 0) {
  throw "javac failed with exit code $LASTEXITCODE"
}

Write-Host ""
Write-Host "Launching Swing GUI..."
java -cp "out;lib/*" rhit.csse.csse374.linter.presentation.gui.LinterGuiMain
if ($LASTEXITCODE -ne 0) {
  throw "java failed with exit code $LASTEXITCODE"
}

