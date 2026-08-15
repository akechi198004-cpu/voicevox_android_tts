package com.example.voicevoxtts;

import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VoicevoxTtsService extends TextToSpeechService {
    private static final String TAG = "VoicevoxTtsService";
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    @Override
    protected String[] onGetLanguage() {
        return new String[]{Locale.JAPAN.getISO3Language(), Locale.JAPAN.getISO3Country(), ""};
    }

    @Override
    protected int onIsLanguageAvailable(String lang, String country, String variant) {
        if (lang == null) return TextToSpeech.LANG_NOT_SUPPORTED;
        if ("jpn".equalsIgnoreCase(lang) || "ja".equalsIgnoreCase(lang)) {
            if (country == null || country.isEmpty()) return TextToSpeech.LANG_AVAILABLE;
            if ("JPN".equalsIgnoreCase(country) || "JP".equalsIgnoreCase(country)) {
                return TextToSpeech.LANG_COUNTRY_AVAILABLE;
            }
            return TextToSpeech.LANG_AVAILABLE;
        }
        return TextToSpeech.LANG_NOT_SUPPORTED;
    }

    @Override
    protected int onLoadLanguage(String lang, String country, String variant) {
        return onIsLanguageAvailable(lang, country, variant);
    }

    @Override
    protected void onStop() {
        stopped.set(true);
    }

    @Override
    protected void onSynthesizeText(SynthesisRequest request, SynthesisCallback callback) {
        stopped.set(false);
        CharSequence cs = request.getCharSequenceText();
        String text = cs == null ? "" : cs.toString();
        if (text.trim().isEmpty()) {
            callback.start(24000, AudioFormat.ENCODING_PCM_16BIT, 1);
            callback.done();
            return;
        }

        int styleId = Prefs.getStyleId(this);
        try {
            byte[] wav = VoicevoxRuntime.get(this).synthesize(text, styleId, request.getSpeechRate());
            if (stopped.get()) return;

            WavPcm pcm = WavPcm.parse(wav);
            if (callback.start(pcm.sampleRate, AudioFormat.ENCODING_PCM_16BIT, pcm.channelCount)
                    != TextToSpeech.SUCCESS) {
                Log.e(TAG, "callback.start failed");
                callback.error(TextToSpeech.ERROR_OUTPUT);
                return;
            }

            int remaining = pcm.dataLength;
            int offset = pcm.dataOffset;
            int max = Math.max(1024, callback.getMaxBufferSize());
            while (remaining > 0 && !stopped.get()) {
                int n = Math.min(max, remaining);
                int result = callback.audioAvailable(wav, offset, n);
                if (result != TextToSpeech.SUCCESS) {
                    Log.e(TAG, "callback.audioAvailable failed: " + result);
                    callback.error(TextToSpeech.ERROR_OUTPUT);
                    return;
                }
                offset += n;
                remaining -= n;
            }

            if (!stopped.get()) callback.done();
        } catch (Throwable t) {
            Log.e(TAG, "Synthesis failed", t);
            callback.error(TextToSpeech.ERROR_SYNTHESIS);
        }
    }
}
