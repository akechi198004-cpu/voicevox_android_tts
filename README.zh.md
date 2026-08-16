# VOICEVOX Android TTS Engine

[日本語](README.md) | [中文](README.zh.md) | [English](README.en.md)

独立安装的 Android 系统 TTS 引擎。把 VOICEVOX Core 封装成标准 `TextToSpeechService`，现有业务 App 继续用 Android `TextToSpeech` API，语音在设备本地合成，不经过服务器。

```text
业务 App
    ↓
Android TextToSpeech API
    ↓
本应用（VOICEVOX TTS 検証）
    ↓
VOICEVOX Core（本地）
    ↓
PCM → 系统播放
```

安装本 APK，在系统 TTS 设置中选为默认引擎后，其他 App 无需改服务器 API，也无需接收 WAV。

## 功能

- 作为系统 TTS Engine 注册（`android.intent.action.TTS_SERVICE`）
- 仅支持日语 `ja-JP`
- 本地合成，不启动 HTTP / 不调用远端 VOICEVOX
- 在本应用中选择默认话者后，其他 App 的标准 `speak()` 会使用该话者
- 测试页：朗读、Core 初始化、打开系统 TTS 设置、许可证、终端信息
- 不覆盖 Google TTS；可随时在系统设置中切回

### 话者

只开放白名单中的平和ノーマル。VVM 里的其他 Style 不会出现在产品中。

| 话者 | style ID | 模型 | クレジット |
|------|----------|------|------------|
| WhiteCUL | 23 | `8.vvm` | VOICEVOX:WhiteCUL |
| 四国めたん | 2 | `0.vvm` | VOICEVOX:四国めたん |
| ずんだもん | 3 | `0.vvm` | VOICEVOX:ずんだもん |
| 春日部つむぎ | 8 | `0.vvm` | VOICEVOX:春日部つむぎ |
| 雨晴はう | 10 | `0.vvm` | VOICEVOX:雨晴はう |
| 玄野武宏 | 11 | `4.vvm` | VOICEVOX:玄野武宏 |
| 剣崎雌雄 | 21 | `4.vvm` | VOICEVOX:剣崎雌雄 |
| 女声1〜6 / 男声1〜3 | 10005 等 | `n0.vvm` | VOICEVOX Nemo |

ずんだもん可通过 `ApprovedVoices.SHOW_ZUNDAMON` 隐藏。

## 运行环境

- ABI：`arm64-v8a` only
- minSdk 26 / targetSdk 35
- 面向固定业务平板（如 Android 14 左右的 ARM64 终端）
- VOICEVOX Core Java/Android **0.17.0**
- VOICEVOX ONNX Runtime Android arm64 CPU **1.23.2**
- VVM **0.17.0**（`0.vvm` / `4.vvm` / `8.vvm` / `n0.vvm`）
- Open JTalk 词典 1.11

应用 ID：`com.example.voicevoxtts`  
应用名：`VOICEVOX TTS 検証`

## 准备依赖

源码包不内置 VOICEVOX 二进制和 VVM。构建前用官方 downloader 获取，并确认利用条款。

Windows：

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\prepare-voicevox.ps1
```

Linux / macOS：

```bash
./scripts/prepare-voicevox.sh
```

脚本会下载：

1. Core 0.17.0 Android Java 包
2. `libvoicevox_onnxruntime.so`（arm64 1.23.2）
3. Open JTalk 词典
4. `0.vvm` / `4.vvm` / `8.vvm` / `n0.vvm`

官方 downloader 会要求确认 VOICEVOX / VVM 利用条款。

## 构建

用 Android Studio 打开本目录，Sync 后 Build APK。

或命令行（需 JDK 17+，本机可用 Android Studio JBR）：

```bash
export JAVA_HOME=/path/to/android-studio/jbr
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew assembleDebug
```

Windows 也可用 `.\scripts\build-debug.ps1`，Linux/macOS 可用 `./scripts/build-debug.sh`。

产物：

```text
app/build/outputs/apk/debug/app-debug.apk
```

APK 约 260MB（含词典与 4 个 VVM）。

## 部署

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

1. 打开 `VOICEVOX TTS 検証`
2. 选择话者（角色音声或 VOICEVOX Nemo）
3. 点「Core初期化テスト」，确认 Core 能启动
4. 点「読み上げテスト」，确认本引擎能朗读
5. 点「システムTTS設定を開く」，把系统默认 TTS 设为本应用
6. 用任意已有业务 App 播日语，确认走本地 VOICEVOX

未把本引擎设为系统默认时，测试页仍会指定 `com.example.voicevoxtts` 进行朗读。

## 验证

```bash
adb logcat -s VoicevoxRuntime VoicevoxTtsService
```

成功时可以看到类似：

```text
VoicevoxTTS:
core init = ...ms
model load = ...ms (8.vvm)
chars = 10
style = WhiteCUL / id=23
synthesis = ...ms
audio = ...s
```

系统是否识别为本 TTS Engine：

```bash
adb shell cmd package query-services --brief -a android.intent.action.TTS_SERVICE
```

应出现：

```text
com.example.voicevoxtts/.VoicevoxTtsService
```

## 现有业务 App

业务侧继续：

```java
new TextToSpeech(context, listener);
tts.setLanguage(Locale.JAPAN);
tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
```

推荐：在系统设置中把本应用设为默认 TTS，业务代码不用改。

也可以显式指定本引擎：

```java
new TextToSpeech(context, listener, "com.example.voicevoxtts");
```

架构保持：

```text
服务器 → 日文文本 → Android TextToSpeech → 本机 VOICEVOX → 播放
```

若业务 App `targetSdkVersion >= 30`，系统设置能看到本引擎但 App 绑定失败，在**业务 App** 的 `AndroidManifest.xml` 增加：

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.TTS_SERVICE" />
    </intent>
</queries>
```

不需要改服务器 API。

## 许可证

本应用内「第三者ソフトウェア・ライセンス」页面会显示必要クレジット。完整说明见 `THIRD_PARTY_NOTICES.md`。

- VOICEVOX Core 0.17.0：MIT（`app/src/main/assets/licenses/VOICEVOX_CORE_LICENSE.txt`）
- 生成音声须遵守各角色音声库条款，并标注 VOICEVOX
- WhiteCUL / 四国めたん / ずんだもん / 春日部つむぎ / 雨晴はう / 玄野武宏 / 剣崎雌雄：按官方条款标注クレジット后，商用・非商用可利用
- VOICEVOX Nemo：标注 `VOICEVOX Nemo` 后，商用・非商用可利用

正式交付前请再核对最新官方条款。
