# VOICEVOX Android TTS Engine

[日本語](README.md) | [中文](README.zh.md) | [English](README.en.md)

単体でインストールできる Android システム TTS エンジンです。VOICEVOX Core を標準の `TextToSpeechService` として提供します。既存の業務アプリは Android `TextToSpeech` API のまま使え、音声は端末ローカルで合成します。サーバーは使いません。

```text
業務アプリ
    ↓
Android TextToSpeech API
    ↓
本アプリ（VOICEVOX TTS 検証）
    ↓
VOICEVOX Core（ローカル）
    ↓
PCM → システム再生
```

本 APK をインストールし、システムの TTS 設定でデフォルトエンジンに指定すれば、他アプリはサーバー API を変えず、WAV を受け取る必要もありません。

## 機能

- システム TTS Engine として登録（`android.intent.action.TTS_SERVICE`）
- 対応言語は日本語 `ja-JP` のみ
- ローカル合成。HTTP サーバーは起動せず、遠隔 VOICEVOX も呼びません
- 本アプリで選んだ話者が、他アプリの標準 `speak()` のデフォルトになります
- テスト画面：読み上げ、Core 初期化、システム TTS 設定、ライセンス、端末情報
- Google TTS は上書きしません。システム設定からいつでも戻せます

### 話者

各キャラクターは落ち着いたノーマルのみです。

| 話者 | style ID | モデル | クレジット |
|------|----------|--------|------------|
| WhiteCUL | 23 | `8.vvm` | VOICEVOX:WhiteCUL |
| 四国めたん | 2 | `0.vvm` | VOICEVOX:四国めたん |
| 玄野武宏 | 11 | `4.vvm` | VOICEVOX:玄野武宏 |
| No.7 | 29 | `6.vvm` | VOICEVOX:No.7 |

## 動作環境

- ABI：`arm64-v8a` のみ
- minSdk 26 / targetSdk 35
- 固定の業務用タブレット（Android 14 前後の ARM64 端末）を想定
- VOICEVOX Core Java/Android **0.17.0**
- VOICEVOX ONNX Runtime Android arm64 CPU **1.23.2**
- VVM **0.17.0**（`0.vvm` / `4.vvm` / `6.vvm` / `8.vvm`）
- Open JTalk 辞書 1.11

アプリケーション ID：`com.example.voicevoxtts`  
アプリ名：`VOICEVOX TTS 検証`

## 依存関係の準備

ソース一式には VOICEVOX のバイナリと VVM を同梱していません。ビルド前に公式ダウンローダーで取得し、利用規約を確認してください。

Windows：

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\prepare-voicevox.ps1
```

Linux / macOS：

```bash
./scripts/prepare-voicevox.sh
```

スクリプトが取得するもの：

1. Core 0.17.0 Android Java パッケージ
2. `libvoicevox_onnxruntime.so`（arm64 1.23.2）
3. Open JTalk 辞書
4. `0.vvm` / `4.vvm` / `6.vvm` / `8.vvm`

公式ダウンローダーは VOICEVOX / VVM の利用規約への同意を求めます。

## ビルド

Android Studio でこのディレクトリを開き、Sync 後に APK をビルドします。

コマンドラインでもビルドできます（JDK 17+。Android Studio の JBR でも可）：

```bash
export JAVA_HOME=/path/to/android-studio/jbr
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew assembleDebug
```

Windows は `.\scripts\build-debug.ps1`、Linux/macOS は `./scripts/build-debug.sh` も使えます。

成果物：

```text
app/build/outputs/apk/debug/app-debug.apk
```

APK は約 260MB です（辞書と VVM 4 本を含みます）。

## 配備

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

1. `VOICEVOX TTS 検証` を開く
2. 話者を選ぶ（WhiteCUL / 四国めたん / 玄野武宏 / No.7）
3. 「Core初期化テスト」で Core が起動することを確認する
4. 「読み上げテスト」で本エンジンが読めることを確認する
5. 「システムTTS設定を開く」から、システムのデフォルト TTS を本アプリにする
6. 既存の業務アプリで日本語を再生し、ローカル VOICEVOX になっていることを確認する

システムデフォルトにしていなくても、テスト画面は `com.example.voicevoxtts` を指定して読み上げます。

## 検証

```bash
adb logcat -s VoicevoxRuntime VoicevoxTtsService
```

成功時の例：

```text
VoicevoxTTS:
core init = ...ms
model load = ...ms (8.vvm)
chars = 10
style = WhiteCUL / id=23
synthesis = ...ms
audio = ...s
```

システムが本 TTS Engine を認識しているか：

```bash
adb shell cmd package query-services --brief -a android.intent.action.TTS_SERVICE
```

次が出れば認識されています。

```text
com.example.voicevoxtts/.VoicevoxTtsService
```

## 既存の業務アプリ

業務側はそのまま使えます。

```java
new TextToSpeech(context, listener);
tts.setLanguage(Locale.JAPAN);
tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
```

推奨：システムの TTS 設定で本アプリをデフォルトにする。業務コードは変更不要です。

エンジンを明示指定することもできます。

```java
new TextToSpeech(context, listener, "com.example.voicevoxtts");
```

構成は次のままです。

```text
サーバー → 日本語テキスト → Android TextToSpeech → 端末上の VOICEVOX → 再生
```

業務アプリの `targetSdkVersion >= 30` で、システム設定には本エンジンが見えるのにアプリからバインドできない場合は、**業務アプリ**の `AndroidManifest.xml` に次を追加します。

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.TTS_SERVICE" />
    </intent>
</queries>
```

サーバー API の変更は不要です。

## ライセンス

アプリ内の「第三者ソフトウェア・ライセンス」に必要なクレジットを表示します。詳細は `THIRD_PARTY_NOTICES.md` を参照してください。

- VOICEVOX Core 0.17.0：MIT（`app/src/main/assets/licenses/VOICEVOX_CORE_LICENSE.txt`）
- 生成音声は各音声ライブラリの規約に従い、VOICEVOX 利用が分かるクレジットが必要です
- WhiteCUL / 四国めたん / 玄野武宏：公式のクレジット表記があれば商用・非商用で利用可能
- No.7：個人の非商用利用は可能。その他の商用利用は No.7 製作委員会への事前確認が必要

正式導入前に、最新の公式規約を再確認してください。
