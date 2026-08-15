package com.example.voicevoxtts;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    private static final String NAME = "voicevox_tts_poc";
    private static final String KEY_STYLE_ID = "style_id";
    static final int DEFAULT_STYLE_ID = 23; // WhiteCUL / ノーマル

    static int getStyleId(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getInt(KEY_STYLE_ID, DEFAULT_STYLE_ID);
    }

    static void setStyleId(Context context, int styleId) {
        SharedPreferences prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_STYLE_ID, styleId).commit();
    }

    private Prefs() {}
}
