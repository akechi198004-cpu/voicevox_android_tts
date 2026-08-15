$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$GradleVersion = "8.9"
$GradleHome = Join-Path $Root ".gradle-dist\gradle-$GradleVersion"
$GradleExe = Join-Path $GradleHome "bin\gradle.bat"

if (-not (Test-Path (Join-Path $Root "local.properties"))) {
    $Sdk = $env:ANDROID_HOME
    if (-not $Sdk) { $Sdk = $env:ANDROID_SDK_ROOT }
    if (-not $Sdk) { $Sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk" }
    if (-not (Test-Path $Sdk)) { throw "Android SDK not found. Set ANDROID_HOME." }
    $Escaped = $Sdk.Replace('\', '\\')
    Set-Content -Path (Join-Path $Root "local.properties") -Value "sdk.dir=$Escaped"
}

if (-not (Test-Path $GradleExe)) {
    $Zip = Join-Path $Root ".gradle-dist\gradle-$GradleVersion-bin.zip"
    New-Item -ItemType Directory -Force (Split-Path -Parent $Zip) | Out-Null
    Invoke-WebRequest "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip" -OutFile $Zip
    Expand-Archive $Zip (Split-Path -Parent $Zip) -Force
}

Push-Location $Root
try {
    & $GradleExe --no-daemon :app:assembleDebug
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
    Write-Host "APK: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Green
} finally { Pop-Location }
