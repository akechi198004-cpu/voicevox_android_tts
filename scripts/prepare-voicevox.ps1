$ErrorActionPreference = "Stop"

$CoreVersion = "0.17.0"
$ModelsVersion = "0.17.0"
$OrtVersion = "1.23.2"

$Root = Split-Path -Parent $PSScriptRoot
$Tmp = Join-Path $Root ".voicevox-download"
$MavenOut = Join-Path $Root "third_party\maven"
$JniOut = Join-Path $Root "app\src\main\jniLibs\arm64-v8a"
$AssetsOut = Join-Path $Root "app\src\main\assets\voicevox"

Write-Host "== VOICEVOX Android TTS POC dependency setup ==" -ForegroundColor Cyan
Write-Host "Core $CoreVersion / VVM $ModelsVersion / ONNX Runtime $OrtVersion"
Write-Host "The official downloader will ask you to accept VOICEVOX model terms."

Remove-Item $Tmp -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $Tmp, $MavenOut, $JniOut, $AssetsOut | Out-Null

# 1) Official Java/Android Maven package
$JavaZip = Join-Path $Tmp "java_packages.zip"
$JavaDir = Join-Path $Tmp "java_packages"
$JavaUrl = "https://github.com/VOICEVOX/voicevox_core/releases/download/$CoreVersion/java_packages.zip"
Write-Host "[1/4] Downloading official Java package..."
Invoke-WebRequest $JavaUrl -OutFile $JavaZip
Expand-Archive -Path $JavaZip -DestinationPath $JavaDir -Force

$Pom = Get-ChildItem $JavaDir -Recurse -Filter "voicevoxcore-android-$CoreVersion.pom" | Select-Object -First 1
if (-not $Pom) { throw "voicevoxcore-android $CoreVersion POM not found in java_packages.zip" }
$RepoRoot = $Pom.Directory.FullName
for ($i = 0; $i -lt 5; $i++) { $RepoRoot = Split-Path -Parent $RepoRoot }
$GroupSrc = Join-Path $RepoRoot "jp\hiroshiba\voicevoxcore"
if (-not (Test-Path $GroupSrc)) { throw "Cannot locate Maven group root: $GroupSrc" }
Remove-Item (Join-Path $MavenOut "jp") -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force (Join-Path $MavenOut "jp\hiroshiba") | Out-Null
Copy-Item $GroupSrc (Join-Path $MavenOut "jp\hiroshiba\voicevoxcore") -Recurse -Force

# 2) Android arm64 VOICEVOX ONNX Runtime
$OrtTgz = Join-Path $Tmp "voicevox_onnxruntime.tgz"
$OrtDir = Join-Path $Tmp "onnxruntime"
$OrtUrl = "https://github.com/VOICEVOX/onnxruntime-builder/releases/download/voicevox_onnxruntime-$OrtVersion/voicevox_onnxruntime-android-arm64-$OrtVersion.tgz"
Write-Host "[2/4] Downloading Android arm64 VOICEVOX ONNX Runtime..."
Invoke-WebRequest $OrtUrl -OutFile $OrtTgz
New-Item -ItemType Directory -Force $OrtDir | Out-Null
& tar.exe -xzf $OrtTgz -C $OrtDir
if ($LASTEXITCODE -ne 0) { throw "tar extraction failed" }
$OrtSo = Get-ChildItem $OrtDir -Recurse -Filter "libvoicevox_onnxruntime.so" | Select-Object -First 1
if (-not $OrtSo) { throw "libvoicevox_onnxruntime.so not found" }
Copy-Item $OrtSo.FullName (Join-Path $JniOut "libvoicevox_onnxruntime.so") -Force

# 3) Official downloader: only the two POC VVMs and Open JTalk dict
$Downloader = Join-Path $Tmp "download-windows-x64.exe"
$DownloaderUrl = "https://github.com/VOICEVOX/voicevox_core/releases/download/$CoreVersion/download-windows-x64.exe"
$RuntimeDir = Join-Path $Tmp "runtime"
Write-Host "[3/4] Downloading official VOICEVOX downloader..."
Invoke-WebRequest $DownloaderUrl -OutFile $Downloader
Write-Host "The following command may ask for terms acceptance." -ForegroundColor Yellow
& $Downloader -o $RuntimeDir --only models dict --models-version $ModelsVersion --models-pattern '[08].vvm'
if ($LASTEXITCODE -ne 0) { throw "VOICEVOX downloader failed with $LASTEXITCODE" }

$ModelsDst = Join-Path $AssetsOut "models"
$DictDst = Join-Path $AssetsOut "dict"
New-Item -ItemType Directory -Force $ModelsDst, $DictDst | Out-Null
foreach ($Name in @("0.vvm", "8.vvm")) {
    $Model = Get-ChildItem $RuntimeDir -Recurse -Filter $Name | Where-Object { $_.FullName -match "vvms" } | Select-Object -First 1
    if (-not $Model) { throw "Model $Name not found" }
    Copy-Item $Model.FullName (Join-Path $ModelsDst $Name) -Force
}
$Dict = Get-ChildItem $RuntimeDir -Recurse -Directory -Filter "open_jtalk_dic_utf_8-1.11" | Select-Object -First 1
if (-not $Dict) { throw "Open JTalk dictionary not found" }
Remove-Item (Join-Path $DictDst "open_jtalk_dic_utf_8-1.11") -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item $Dict.FullName (Join-Path $DictDst "open_jtalk_dic_utf_8-1.11") -Recurse -Force

Write-Host "[4/4] Verifying..."
$Checks = @(
    (Join-Path $JniOut "libvoicevox_onnxruntime.so"),
    (Join-Path $ModelsDst "0.vvm"),
    (Join-Path $ModelsDst "8.vvm"),
    (Join-Path $DictDst "open_jtalk_dic_utf_8-1.11")
)
foreach ($Path in $Checks) {
    if (-not (Test-Path $Path)) { throw "Missing: $Path" }
    Write-Host "  OK  $Path"
}

Write-Host "DONE. You can now build the Android project." -ForegroundColor Green
