# VOICEVOX Android TTS Engine

[日本語](README.md) | [中文](README.zh.md) | [English](README.en.md)

A standalone Android system TTS engine. It wraps VOICEVOX Core as a standard `TextToSpeechService`. Existing business apps keep using the Android `TextToSpeech` API. Speech is synthesized on the device. No server is involved.

```text
Business app
    ↓
Android TextToSpeech API
    ↓
This app (VOICEVOX TTS 検証)
    ↓
VOICEVOX Core (on device)
    ↓
PCM → system playback
```

Install this APK and set it as the default TTS engine. Other apps do not need a new server API and do not need to receive WAV files.

## Features

- Registers as a system TTS engine (`android.intent.action.TTS_SERVICE`)
- Japanese `ja-JP` only
- Local synthesis. No HTTP server. No remote VOICEVOX
- The speaker selected in this app becomes the default for other apps' standard `speak()` calls
- Test screen: speak, initialize Core, open system TTS settings, licenses, device info
- Does not replace Google TTS. You can switch back in system settings at any time

### Speakers

Only approved calm Normal styles are exposed. Other styles inside a VVM stay hidden.

| Speaker | style ID | Model | Credit |
|---------|----------|-------|--------|
| WhiteCUL | 23 | `8.vvm` | VOICEVOX:WhiteCUL |
| 四国めたん | 2 | `0.vvm` | VOICEVOX:四国めたん |
| ずんだもん | 3 | `0.vvm` | VOICEVOX:ずんだもん |
| 春日部つむぎ | 8 | `0.vvm` | VOICEVOX:春日部つむぎ |
| 雨晴はう | 10 | `0.vvm` | VOICEVOX:雨晴はう |
| 玄野武宏 | 11 | `4.vvm` | VOICEVOX:玄野武宏 |
| 剣崎雌雄 | 21 | `4.vvm` | VOICEVOX:剣崎雌雄 |
| Female 1–6 / Male 1–3 | 10005 etc. | `n0.vvm` | VOICEVOX Nemo |

ずんだもん can be hidden with `ApprovedVoices.SHOW_ZUNDAMON`.

## Requirements

- ABI: `arm64-v8a` only
- minSdk 26 / targetSdk 35
- Intended for fixed business tablets (ARM64 devices around Android 14)
- VOICEVOX Core Java/Android **0.17.0**
- VOICEVOX ONNX Runtime Android arm64 CPU **1.23.2**
- VVM **0.17.0** (`0.vvm` / `4.vvm` / `8.vvm` / `n0.vvm`)
- Open JTalk dictionary 1.11

Application ID: `com.example.voicevoxtts`  
App name: `VOICEVOX TTS 検証`

## Prepare dependencies

The source tree does not ship VOICEVOX binaries or VVM files. Download them with the official downloader and accept the terms before building.

Windows:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\prepare-voicevox.ps1
```

Linux / macOS:

```bash
./scripts/prepare-voicevox.sh
```

The script downloads:

1. Core 0.17.0 Android Java package
2. `libvoicevox_onnxruntime.so` (arm64 1.23.2)
3. Open JTalk dictionary
4. `0.vvm` / `4.vvm` / `8.vvm` / `n0.vvm`

The official downloader will ask you to accept the VOICEVOX / VVM terms.

## Build

Open this directory in Android Studio, sync Gradle, then build the APK.

Or use the command line (JDK 17+; Android Studio JBR is fine):

```bash
export JAVA_HOME=/path/to/android-studio/jbr
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew assembleDebug
```

On Windows you can also run `.\scripts\build-debug.ps1`. On Linux/macOS you can run `./scripts/build-debug.sh`.

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The APK is about 260MB (dictionary plus four VVM files).

## Deploy

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

1. Open `VOICEVOX TTS 検証`
2. Select a speaker (character voices or VOICEVOX Nemo)
3. Tap **Core初期化テスト** and confirm Core starts
4. Tap **読み上げテスト** and confirm this engine speaks
5. Tap **システムTTS設定を開く** and set this app as the system default TTS
6. Play Japanese from an existing business app and confirm it uses local VOICEVOX

The test screen still binds to `com.example.voicevoxtts` even if this engine is not the system default.

## Verify

```bash
adb logcat -s VoicevoxRuntime VoicevoxTtsService
```

On success you should see something like:

```text
VoicevoxTTS:
core init = ...ms
model load = ...ms (8.vvm)
chars = 10
style = WhiteCUL / id=23
synthesis = ...ms
audio = ...s
```

Check that Android recognizes this TTS engine:

```bash
adb shell cmd package query-services --brief -a android.intent.action.TTS_SERVICE
```

You should see:

```text
com.example.voicevoxtts/.VoicevoxTtsService
```

## Existing business apps

Keep using the standard API:

```java
new TextToSpeech(context, listener);
tts.setLanguage(Locale.JAPAN);
tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
```

Recommended: set this app as the default TTS in system settings. No business-code change is required.

You can also pin this engine explicitly:

```java
new TextToSpeech(context, listener, "com.example.voicevoxtts");
```

The architecture stays:

```text
Server → Japanese text → Android TextToSpeech → on-device VOICEVOX → playback
```

If the business app has `targetSdkVersion >= 30`, the system settings show this engine, but the app cannot bind to it, add this to the **business app** `AndroidManifest.xml`:

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.TTS_SERVICE" />
    </intent>
</queries>
```

No server API change is required.

## License

The in-app **第三者ソフトウェア・ライセンス** screen shows the required credits. See `THIRD_PARTY_NOTICES.md` for details.

- VOICEVOX Core 0.17.0: MIT (`app/src/main/assets/licenses/VOICEVOX_CORE_LICENSE.txt`)
- Generated audio must follow each character voice-library terms and credit VOICEVOX
- WhiteCUL / 四国めたん / ずんだもん / 春日部つむぎ / 雨晴はう / 玄野武宏 / 剣崎雌雄: commercial and non-commercial use allowed with the official credit
- VOICEVOX Nemo: commercial and non-commercial use allowed with the credit `VOICEVOX Nemo`

Re-check the latest official terms before production use.
