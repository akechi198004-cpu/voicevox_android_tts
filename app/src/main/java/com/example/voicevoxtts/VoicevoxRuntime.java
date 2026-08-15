package com.example.voicevoxtts;

import android.content.Context;
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

    synchronized void ensureReadyForStyle(int styleId) throws Exception {
        if (synthesizer == null) initializeCore();
        VoiceOptions.Option option = VoiceOptions.byStyleId(styleId);
        if (!loadedModels.contains(option.modelAsset)) {
            File model = installer.installModel(option.modelAsset);
            long begin = System.currentTimeMillis();
            try (VoiceModelFile voiceModel = new VoiceModelFile(model.getAbsolutePath())) {
                synthesizer.loadVoiceModel(voiceModel).perform();
            }
            loadedModels.add(option.modelAsset);
            Log.i(TAG, "Loaded " + option.modelAsset + " in " + (System.currentTimeMillis() - begin) + " ms");
        }
    }

    synchronized byte[] synthesize(String text, int styleId, int androidSpeechRate) throws Exception {
        ensureReadyForStyle(styleId);
        long begin = System.currentTimeMillis();

        AudioQuery query = synthesizer.createAudioQuery(text, styleId);
        double requestedRate = androidSpeechRate > 0 ? androidSpeechRate / 100.0 : 1.0;
        // Android's 100 = normal. Keep the POC in a conservative range.
        query.speedScale = Math.max(0.5, Math.min(2.0, query.speedScale * requestedRate));
        query.outputSamplingRate = 24000;
        query.outputStereo = false;

        byte[] wav = synthesizer.synthesis(query, styleId).perform();
        Log.i(TAG, "Synthesized style=" + styleId + ", chars=" + text.length()
                + ", wav=" + wav.length + " bytes, elapsed=" + (System.currentTimeMillis() - begin) + " ms");
        return wav;
    }

    synchronized String diagnosticSummary() {
        if (synthesizer == null) return "Core not initialized yet";
        return "Core initialized / models=" + loadedModels;
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
        Log.i(TAG, "Core initialized in " + (System.currentTimeMillis() - begin)
                + " ms; nativeLibraryDir=" + context.getApplicationInfo().nativeLibraryDir);
    }
}
