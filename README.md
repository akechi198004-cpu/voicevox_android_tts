# VOICEVOX Android TTS Engine POC

这是一个**最小验证项目**：把 VOICEVOX Core 0.17.0 封装成 Android 标准 `TextToSpeechService`，让现有业务 App 继续使用 Android `TextToSpeech` API，而不是把 TTS 集中到服务器。

## 这个 POC 验证什么

- 安装 APK 后，Android 能否把它识别为一个 TTS Engine。
- 在设备本地用 VOICEVOX Core 合成日语，不经过服务器。
- WhiteCUL / 四国めたん能否在目标富士通 / FCNT ARM64 设备上稳定运行。
- 实测首次初始化、首次模型加载、20～30 字短句合成耗时与内存/CPU。

## 固定版本

- VOICEVOX Core Java/Android: **0.17.0**
- VOICEVOX ONNX Runtime Android arm64 CPU: **1.23.2**
- VVM: **0.17.0**
- `0.vvm`: 四国めたん等
- `8.vvm`: WhiteCUL
- ABI: **arm64-v8a only**
- minSdk: **26**

## 第一步：准备官方依赖

项目本身没有把 VOICEVOX 的二进制和 VVM 模型直接塞进 ZIP。这样一方面文件小，另一方面你会在官方 downloader 阶段亲自确认模型利用条款。

Windows PowerShell：

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\prepare-voicevox.ps1
```

Ubuntu / macOS：

```bash
./scripts/prepare-voicevox.sh
```

脚本会从 VOICEVOX 官方 GitHub Release 获取：

1. `java_packages.zip`（Core 0.17.0 Android Java API）
2. `voicevox_onnxruntime-android-arm64-1.23.2.tgz`
3. 官方 downloader
4. Open JTalk dictionary
5. `0.vvm` 和 `8.vvm`

**官方 downloader 会要求确认 VOICEVOX/VVM 的利用条款，这是正常的。**

## 第二步：构建

最方便：直接 Android Studio 打开本目录，等 Gradle Sync 后 Build APK。

也可以命令行：

Windows：

```powershell
.\scripts\build-debug.ps1
```

Linux/macOS：

```bash
./scripts/build-debug.sh
```

生成：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 第三步：装到富士通设备

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

然后：

1. 打开 `VOICEVOX TTS POC`。
2. 选择 WhiteCUL 或四国めたん。
3. 点 `VOICEVOX Core 初期化だけ試す`，先看 Core 能否初始化。
4. 点 `このTTSエンジンで読み上げテスト`。
5. 如果设备 ROM 不允许 App 主动指定未启用的 TTS Engine，则点 `Android の TTS 設定を開く`，在系统 TTS 设置中选择 `VOICEVOX TTS POC` 后再试。

ADB 看日志：

```bash
adb logcat -s VoicevoxRuntime VoicevoxTtsService
```

会看到类似：

```text
Core initialized in ... ms
Loaded 8.vvm in ... ms
Synthesized style=23, chars=..., wav=... bytes, elapsed=... ms
```

这几个数字就是最值得记录的 POC 结果。

## 现有业务 App 如何替换

如果业务 App 目前就是标准 Android `TextToSpeech`：

```java
new TextToSpeech(context, listener)
```

正式方案可以有两种：

- 在设备系统设置里把本引擎设为默认 TTS；业务代码基本不动。
- 业务 App 明确指定这个 TTS package：

```java
new TextToSpeech(context, listener, "com.example.voicevoxtts")
```

这样服务器仍然只返回 text，架构继续保持：

```text
Server -> text -> Android -> 本地 TTS -> 播放
```

不会变成所有终端集中请求一台 VOICEVOX Server。

### Android 11+ 调用侧的 package visibility

如果现有业务 App `targetSdkVersion >= 30`，并且发现系统 TTS 设置能看到本 POC、但业务 App 无法发现/绑定第三方 TTS Engine，可在**业务 App**的 `AndroidManifest.xml` 中加入：

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.TTS_SERVICE" />
    </intent>
</queries>
```

这属于调用侧兼容处理，不需要改服务器 API。

## POC 中做了什么 / 没做什么

已做：

- Android `TextToSpeechService`
- VOICEVOX Core 本地推理
- WAV -> PCM16 -> `SynthesisCallback`
- WhiteCUL / 四国めたん Style 选择
- Android `setSpeechRate()` 的基础映射
- arm64-v8a
- Credits 展示

暂未做（正式版应补）：

- Android `Voice` API 的完整 voice 列举 / `setVoice()` 映射
- `setPitch()` 到 VOICEVOX `pitchScale` 的正式映射
- 模型按需安装/升级
- 更完整的初始化失败恢复
- 对两款富士通设备分别做性能回归
- 正式 UI / 第三方软件许可页
- 商业交付前重新核对 WhiteCUL / 四国めたん最新利用规约

## 关键文件

```text
VoicevoxTtsService.java   Android 系统 TTS Engine 入口
VoicevoxRuntime.java      VOICEVOX Core 初始化、模型加载、合成
WavPcm.java               VOICEVOX WAV -> Android PCM callback
MainActivity.java         POC 测试/音色选择页面
prepare-voicevox.*        下载官方依赖和两个 VVM
```

## Credits / License

POC UI 中显示：

- VOICEVOX
- VOICEVOX:WhiteCUL
- VOICEVOX:四国めたん

VOICEVOX Core 0.17.0 为 MIT License，完整 MIT 文本放在：

```text
app/src/main/assets/licenses/VOICEVOX_CORE_LICENSE.txt
```

VVM 与 VOICEVOX ONNX Runtime 有各自利用条款；模型生成音声还必须遵守角色音声库条款。正式商业交付前请重新确认最新条款。
