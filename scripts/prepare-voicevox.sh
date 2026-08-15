#!/usr/bin/env bash
set -euo pipefail

CORE_VERSION=0.17.0
MODELS_VERSION=0.17.0
ORT_VERSION=1.23.2
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$ROOT/.voicevox-download"
MAVEN_OUT="$ROOT/third_party/maven"
JNI_OUT="$ROOT/app/src/main/jniLibs/arm64-v8a"
ASSETS_OUT="$ROOT/app/src/main/assets/voicevox"

rm -rf "$TMP"
mkdir -p "$TMP" "$MAVEN_OUT" "$JNI_OUT" "$ASSETS_OUT/models" "$ASSETS_OUT/dict"

echo "== VOICEVOX Android TTS POC dependency setup =="
echo "Core $CORE_VERSION / VVM $MODELS_VERSION / ONNX Runtime $ORT_VERSION"
echo "The official downloader may ask you to accept VOICEVOX model terms."

# 1) Java/Android Maven package
curl -fL "https://github.com/VOICEVOX/voicevox_core/releases/download/$CORE_VERSION/java_packages.zip" -o "$TMP/java_packages.zip"
unzip -q "$TMP/java_packages.zip" -d "$TMP/java_packages"
POM="$(find "$TMP/java_packages" -name "voicevoxcore-android-$CORE_VERSION.pom" -print -quit)"
[[ -n "$POM" ]] || { echo "voicevoxcore Android POM not found" >&2; exit 1; }
REPO_ROOT="$(dirname "$POM")"
for _ in 1 2 3 4 5; do REPO_ROOT="$(dirname "$REPO_ROOT")"; done
GROUP_SRC="$REPO_ROOT/jp/hiroshiba/voicevoxcore"
[[ -d "$GROUP_SRC" ]] || { echo "Maven group not found: $GROUP_SRC" >&2; exit 1; }
rm -rf "$MAVEN_OUT/jp"
mkdir -p "$MAVEN_OUT/jp/hiroshiba"
cp -a "$GROUP_SRC" "$MAVEN_OUT/jp/hiroshiba/"

# 2) Android arm64 ONNX Runtime
curl -fL "https://github.com/VOICEVOX/onnxruntime-builder/releases/download/voicevox_onnxruntime-$ORT_VERSION/voicevox_onnxruntime-android-arm64-$ORT_VERSION.tgz" -o "$TMP/ort.tgz"
mkdir -p "$TMP/ort"
tar -xzf "$TMP/ort.tgz" -C "$TMP/ort"
ORT_SO="$(find "$TMP/ort" -name 'libvoicevox_onnxruntime.so' -print -quit)"
[[ -n "$ORT_SO" ]] || { echo "libvoicevox_onnxruntime.so not found" >&2; exit 1; }
cp "$ORT_SO" "$JNI_OUT/libvoicevox_onnxruntime.so"

# 3) Platform downloader for models + dict
UNAME_S="$(uname -s)"
UNAME_M="$(uname -m)"
case "$UNAME_S/$UNAME_M" in
  Linux/x86_64) DOWNLOADER=download-linux-x64 ;;
  Linux/aarch64|Linux/arm64) DOWNLOADER=download-linux-arm64 ;;
  Darwin/x86_64) DOWNLOADER=download-osx-x64 ;;
  Darwin/arm64) DOWNLOADER=download-osx-arm64 ;;
  *) echo "Unsupported host for official downloader: $UNAME_S/$UNAME_M" >&2; exit 1 ;;
esac
curl -fL "https://github.com/VOICEVOX/voicevox_core/releases/download/$CORE_VERSION/$DOWNLOADER" -o "$TMP/download"
chmod +x "$TMP/download"
"$TMP/download" -o "$TMP/runtime" --only models dict --models-version "$MODELS_VERSION" --models-pattern '[08].vvm'

for MODEL in 0.vvm 8.vvm; do
  SRC="$(find "$TMP/runtime" -path '*/vvms/*' -name "$MODEL" -print -quit)"
  [[ -n "$SRC" ]] || { echo "Model $MODEL not found" >&2; exit 1; }
  cp "$SRC" "$ASSETS_OUT/models/$MODEL"
done
DICT="$(find "$TMP/runtime" -type d -name 'open_jtalk_dic_utf_8-1.11' -print -quit)"
[[ -n "$DICT" ]] || { echo "Open JTalk dictionary not found" >&2; exit 1; }
rm -rf "$ASSETS_OUT/dict/open_jtalk_dic_utf_8-1.11"
cp -a "$DICT" "$ASSETS_OUT/dict/"

echo "DONE. Build the Android project next."
