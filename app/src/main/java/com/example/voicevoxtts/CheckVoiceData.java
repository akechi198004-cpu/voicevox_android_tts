package com.example.voicevoxtts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;

/**
 * Android TTS framework uses this to decide whether the engine has usable voice data.
 */
public final class CheckVoiceData extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> available = new ArrayList<>();
        available.add("jpn-JPN");
        Intent result = new Intent();
        result.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES, available);
        result.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES, new ArrayList<>());
        setResult(TextToSpeech.Engine.CHECK_VOICE_DATA_PASS, result);
        finish();
    }
}
