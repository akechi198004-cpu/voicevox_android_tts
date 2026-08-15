package com.example.voicevoxtts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private TextToSpeech tts;
    private TextView status;
    private EditText testText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
    }

    private View buildUi() {
        int pad = dp(20);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("VOICEVOX Android TTS Engine POC");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("目标：把 VOICEVOX Core 封装成 Android 标准 TextToSpeechService。\n"
                + "本 POC 仅构建 arm64-v8a，并只准备 WhiteCUL 与四国めたん模型。");
        desc.setTextSize(15);
        desc.setPadding(0, dp(12), 0, dp(16));
        root.addView(desc);

        TextView voiceLabel = new TextView(this);
        voiceLabel.setText("音色 / Style");
        voiceLabel.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(voiceLabel);

        Spinner spinner = new Spinner(this);
        List<VoiceOptions.Option> options = new ArrayList<>(VoiceOptions.ALL);
        ArrayAdapter<VoiceOptions.Option> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, options);
        spinner.setAdapter(adapter);
        int savedStyle = Prefs.getStyleId(this);
        int selected = 0;
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).styleId == savedStyle) selected = i;
        }
        spinner.setSelection(selected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VoiceOptions.Option option = options.get(position);
                Prefs.setStyleId(MainActivity.this, option.styleId);
                setStatus("Selected: " + option.label + " / styleId=" + option.styleId
                        + "\nCredit: " + option.credit);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        root.addView(spinner);

        testText = new EditText(this);
        testText.setText("お客様へのお知らせです。処理が完了しました。");
        testText.setSingleLine(false);
        testText.setMinLines(2);
        testText.setPadding(0, dp(16), 0, dp(12));
        root.addView(testText);

        Button test = button("このTTSエンジンで読み上げテスト");
        test.setOnClickListener(v -> speakWithThisEngine());
        root.addView(test);

        Button settings = button("Android の TTS 設定を開く");
        settings.setOnClickListener(v -> openTtsSettings());
        root.addView(settings);

        Button warmup = button("VOICEVOX Core 初期化だけ試す");
        warmup.setOnClickListener(v -> {
            setStatus("Initializing VOICEVOX Core...");
            new Thread(() -> {
                try {
                    int style = Prefs.getStyleId(this);
                    long start = System.currentTimeMillis();
                    VoicevoxRuntime.get(this).ensureReadyForStyle(style);
                    long ms = System.currentTimeMillis() - start;
                    runOnUiThread(() -> setStatus("Core ready in " + ms + " ms\n"
                            + VoicevoxRuntime.get(this).diagnosticSummary()));
                } catch (Throwable t) {
                    runOnUiThread(() -> setStatus("Core init FAILED:\n" + t));
                }
            }, "voicevox-warmup").start();
        });
        root.addView(warmup);

        status = new TextView(this);
        status.setTextSize(14);
        status.setPadding(0, dp(18), 0, dp(18));
        status.setTextIsSelectable(true);
        root.addView(status);

        TextView credits = new TextView(this);
        credits.setText("Third-party / Credits\n"
                + "VOICEVOX Core 0.17.0 (MIT)\n"
                + "VOICEVOX ONNX Runtime 1.23.2\n"
                + "VOICEVOX:WhiteCUL\n"
                + "VOICEVOX:四国めたん\n\n"
                + "POC用途。正式導入前に各音声ライブラリの最新利用規約を再確認してください。");
        credits.setTextSize(13);
        root.addView(credits);

        return scroll;
    }

    private void speakWithThisEngine() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        setStatus("Binding to TTS engine: " + getPackageName());
        tts = new TextToSpeech(this, result -> {
            if (result != TextToSpeech.SUCCESS) {
                setStatus("TextToSpeech init FAILED: " + result
                        + "\n先确认 APK 已被系统识别成 TTS Engine。");
                return;
            }
            int lang = tts.setLanguage(Locale.JAPAN);
            setStatus("TTS init OK / language result=" + lang + "\nSynthesizing...");
            tts.speak(testText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "voicevox-poc-test");
        }, getPackageName());
    }

    private void openTtsSettings() {
        try {
            startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
        } catch (Throwable ignored) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        b.setLayoutParams(lp);
        b.setGravity(Gravity.CENTER);
        return b;
    }

    private void setStatus(String text) {
        if (status != null) status.setText(text);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
