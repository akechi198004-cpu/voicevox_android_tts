package com.example.voicevoxtts;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.system.Os;
import android.util.Log;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import jp.hiroshiba.voicevoxcore.AudioQuery;
import jp.hiroshiba.voicevoxcore.blocking.Onnxruntime;
import jp.hiroshiba.voicevoxcore.blocking.OpenJtalk;
import jp.hiroshiba.voicevoxcore.blocking.Synthesizer;
import jp.hiroshiba.voicevoxcore.blocking.VoiceModelFile;

final class VoicevoxRuntime {
    private static final String TAG = "VoicevoxRuntime";
    private static volatile VoicevoxRuntime instance;

    private final Context context;
    private Synthesizer synthesizer;
    private AssetInstaller installer;
    private final Set<String> loadedModels = new HashSet<>();

    private long lastCoreInitMs = -1;
    private long lastModelLoadMs = -1;
    private String lastLoadedModel = "";

    static VoicevoxRuntime get(Context context) {
        VoicevoxRuntime local = instance;
        if (local == null) {
            synchronized (VoicevoxRuntime.class) {
                local = instance;
                if (local == null) {
                    local = new VoicevoxRuntime(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    private VoicevoxRuntime(Context context) {
        this.context = context;
    }

    synchronized boolean isCoreReady() {
        return synthesizer != null;
    }

    synchronized void ensureReadyForStyle(int styleId) throws Exception {
        if (synthesizer == null) initializeCore();
        ApprovedVoices.Option option = ApprovedVoices.byStyleId(styleId);
        if (!loadedModels.contains(option.modelAsset)) {
            File model = installer.installModel(option.modelAsset);
            long begin = System.currentTimeMillis();
            try (VoiceModelFile voiceModel = new VoiceModelFile(model.getAbsolutePath())) {
                synthesizer.loadVoiceModel(voiceModel).perform();
            }
            lastModelLoadMs = System.currentTimeMillis() - begin;
            lastLoadedModel = option.modelAsset;
            loadedModels.add(option.modelAsset);
            Log.i(TAG, "Loaded " + option.modelAsset + " in " + lastModelLoadMs + " ms");
        }
    }

    synchronized byte[] synthesize(String text, int styleId, int androidSpeechRate) throws Exception {
        ensureReadyForStyle(styleId);
        ApprovedVoices.Option option = ApprovedVoices.byStyleId(styleId);
        long begin = System.currentTimeMillis();

        AudioQuery query = synthesizer.createAudioQuery(text, styleId);
        double requestedRate = androidSpeechRate > 0 ? androidSpeechRate / 100.0 : 1.0;
        // Android's 100 = normal. Keep the POC in a conservative range.
        query.speedScale = Math.max(0.5, Math.min(2.0, query.speedScale * requestedRate));
        query.outputSamplingRate = 24000;
        query.outputStereo = false;

        byte[] wav = synthesizer.synthesis(query, styleId).perform();
        long synthesisMs = System.currentTimeMillis() - begin;
        WavPcm pcm = WavPcm.parse(wav);
        double audioSec = pcm.dataLength / (double) (pcm.sampleRate * pcm.channelCount * (pcm.bitsPerSample / 8));

        Log.i(TAG, "VoicevoxTTS:\n"
                + "core init = " + lastCoreInitMs + "ms\n"
                + "model load = " + lastModelLoadMs + "ms (" + lastLoadedModel + ")\n"
                + "chars = " + text.length() + "\n"
                + "style = " + option.label + " / id=" + styleId + "\n"
                + "synthesis = " + synthesisMs + "ms\n"
                + "audio = " + String.format("%.2f", audioSec) + "s\n"
                + "wav = " + wav.length + " bytes\n"
                + memorySummary());
        return wav;
    }

    synchronized String diagnosticSummary() {
        if (synthesizer == null) return "未初期化";
        return "準備完了\n"
                + "読み込み済みモデル=" + loadedModels + "\n"
                + "Core初期化=" + lastCoreInitMs + "ms\n"
                + "前回のモデル読み込み=" + lastModelLoadMs + "ms（" + lastLoadedModel + "）\n"
                + memorySummary();
    }

    String memorySummary() {
        Runtime rt = Runtime.getRuntime();
        long javaUsedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long javaMaxMb = rt.maxMemory() / (1024 * 1024);
        long nativeMb = Debug.getNativeHeapAllocatedSize() / (1024 * 1024);
        long availMb = -1;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(info);
            availMb = info.availMem / (1024 * 1024);
        }
        return "メモリ Java=" + javaUsedMb + "MB/" + javaMaxMb + "MB"
                + " NativeHeap=" + nativeMb + "MB"
                + " 利用可能=" + availMb + "MB";
    }

    private void initializeCore() throws Exception {
        installer = new AssetInstaller(context);
        File dict = installer.installDict();

        // OpenJTalk user-dict operations on Android require TMPDIR; setting it up front is harmless
        // and makes the runtime ready for future user-dictionary support.
        Os.setenv("TMPDIR", context.getCacheDir().getAbsolutePath(), true);

        File ort = new File(context.getApplicationInfo().nativeLibraryDir, "libvoicevox_onnxruntime.so");
        if (!ort.isFile()) {
            throw new IllegalStateException("Missing native runtime: " + ort
                    + ". Run scripts/prepare-voicevox.* before building.");
        }

        long begin = System.currentTimeMillis();
        Onnxruntime onnxruntime = Onnxruntime.loadOnce().filename(ort.getAbsolutePath()).perform();
        OpenJtalk openJtalk = new OpenJtalk(dict.getAbsolutePath());
        synthesizer = Synthesizer.builder(onnxruntime, openJtalk)
                .cpuNumThreads(Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors())))
                .build();
        lastCoreInitMs = System.currentTimeMillis() - begin;
        Log.i(TAG, "Core initialized in " + lastCoreInitMs
                + " ms; nativeLibraryDir=" + context.getApplicationInfo().nativeLibraryDir);
    }
}
