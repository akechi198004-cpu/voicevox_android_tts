#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GRADLE_VERSION=8.9
DIST="$ROOT/.gradle-dist"
GRADLE="$DIST/gradle-$GRADLE_VERSION/bin/gradle"

if [[ ! -f "$ROOT/local.properties" ]]; then
  SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
  [[ -d "$SDK" ]] || { echo "Android SDK not found. Set ANDROID_HOME." >&2; exit 1; }
  printf 'sdk.dir=%s\n' "$SDK" > "$ROOT/local.properties"
fi
if [[ ! -x "$GRADLE" ]]; then
  mkdir -p "$DIST"
  curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$DIST/gradle.zip"
  unzip -q "$DIST/gradle.zip" -d "$DIST"
fi
cd "$ROOT"
"$GRADLE" --no-daemon :app:assembleDebug
echo "APK: app/build/outputs/apk/debug/app-debug.apk"
