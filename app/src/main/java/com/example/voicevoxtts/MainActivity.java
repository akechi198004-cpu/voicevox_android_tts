package com.example.voicevoxtts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
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
    private TextView coreStatus;
    private TextView currentVoice;
    private EditText testText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
        refreshStatus();
    }

    private View buildUi() {
        int pad = dp(20);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);
        applyCutoutPadding(scroll, pad);

        TextView title = new TextView(this);
        title.setText("VOICEVOX Android TTSエンジン検証");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("VOICEVOX CoreをAndroid標準のTextToSpeechServiceとして組み込みます。\n"
                + "この検証アプリはarm64-v8aのみ対応。話者：WhiteCUL / 四国めたん / 玄野武宏 / No.7。");
        desc.setTextSize(15);
        desc.setPadding(0, dp(12), 0, dp(16));
        root.addView(desc);

        coreStatus = labeledBlock(root, "VOICEVOX Coreの状態");
        currentVoice = labeledBlock(root, "現在の話者");

        TextView voiceLabel = new TextView(this);
        voiceLabel.setText("話者");
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
                refreshStatus();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        root.addView(spinner);

        testText = new EditText(this);
        testText.setText("お知らせがあります。");
        testText.setSingleLine(false);
        testText.setMinLines(2);
        testText.setPadding(0, dp(16), 0, dp(12));
        root.addView(testText);

        Button test = button("読み上げテスト");
        test.setOnClickListener(v -> speakWithThisEngine());
        root.addView(test);

        Button warmup = button("Core初期化テスト");
        warmup.setOnClickListener(v -> warmupCore());
        root.addView(warmup);

        Button settings = button("システムTTS設定を開く");
        settings.setOnClickListener(v -> openTtsSettings());
        root.addView(settings);

        Button license = button("第三者ソフトウェア・ライセンス");
        license.setOnClickListener(v -> startActivity(new Intent(this, LicenseActivity.class)));
        root.addView(license);

        status = new TextView(this);
        status.setTextSize(14);
        status.setPadding(0, dp(18), 0, dp(18));
        status.setTextIsSelectable(true);
        root.addView(status);

        TextView device = new TextView(this);
        device.setTypeface(Typeface.DEFAULT_BOLD);
        device.setText("端末情報");
        root.addView(device);
        TextView deviceBody = new TextView(this);
        deviceBody.setTextSize(13);
        deviceBody.setTextIsSelectable(true);
        deviceBody.setText(DeviceInfo.summary(this));
        deviceBody.setPadding(0, dp(8), 0, dp(16));
        root.addView(deviceBody);

        TextView credits = new TextView(this);
        credits.setText("クレジット\n"
                + "VOICEVOX Core 0.17.0（MIT）\n"
                + "VOICEVOX ONNX Runtime 1.23.2\n"
                + "VOICEVOX:WhiteCUL\n"
                + "VOICEVOX:四国めたん\n"
                + "VOICEVOX:玄野武宏\n"
                + "VOICEVOX:No.7\n\n"
                + "検証用途です。正式導入前に各音声ライブラリの最新利用規約を再確認してください。");
        credits.setTextSize(13);
        root.addView(credits);

        return scroll;
    }

    private void warmupCore() {
        setStatus("VOICEVOX Coreを初期化しています…");
        new Thread(() -> {
            try {
                int style = Prefs.getStyleId(this);
                long start = System.currentTimeMillis();
                VoicevoxRuntime.get(this).ensureReadyForStyle(style);
                long ms = System.currentTimeMillis() - start;
                runOnUiThread(() -> {
                    refreshStatus();
                    setStatus("初期化完了（" + ms + " ms）\n"
                            + VoicevoxRuntime.get(this).diagnosticSummary());
                });
            } catch (Throwable t) {
                runOnUiThread(() -> setStatus("初期化に失敗しました:\n" + t));
            }
        }, "voicevox-warmup").start();
    }

    private void speakWithThisEngine() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        setStatus("TTSエンジンに接続しています: " + getPackageName());
        tts = new TextToSpeech(this, result -> {
            if (result != TextToSpeech.SUCCESS) {
                setStatus("TTSの初期化に失敗しました: " + result
                        + "\nこのアプリがシステムのTTSエンジンとして認識されているか確認してください。");
                return;
            }
            int lang = tts.setLanguage(Locale.JAPAN);
            VoiceOptions.Option option = VoiceOptions.byStyleId(Prefs.getStyleId(this));
            android.speech.tts.Voice selected = findVoice(tts, option.styleId);
            if (selected != null) {
                tts.setVoice(selected);
            }
            setStatus("TTS初期化成功 / 言語=" + lang
                    + "\n音声=" + (selected != null ? selected.getName() : "デフォルト")
                    + "\n" + option.label + " / スタイルID=" + option.styleId
                    + "\n合成中…");
            tts.speak(testText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "voicevox-poc-test");
        }, getPackageName());
    }

    private android.speech.tts.Voice findVoice(TextToSpeech tts, int styleId) {
        String name = VoicevoxTtsService.VOICE_PREFIX + styleId;
        java.util.Set<android.speech.tts.Voice> voices = tts.getVoices();
        if (voices == null) return null;
        for (android.speech.tts.Voice voice : voices) {
            if (name.equals(voice.getName())) return voice;
        }
        return null;
    }

    private void openTtsSettings() {
        String[] actions = {
                "com.android.settings.TTS_SETTINGS",
                "android.settings.TTS_SETTINGS",
                Settings.ACTION_SETTINGS
        };
        Throwable last = null;
        for (String action : actions) {
            try {
                startActivity(new Intent(action));
                return;
            } catch (Throwable t) {
                last = t;
            }
        }
        setStatus("TTS設定を開けませんでした: " + last);
    }

    private void refreshStatus() {
        VoiceOptions.Option option = VoiceOptions.byStyleId(Prefs.getStyleId(this));
        if (coreStatus != null) {
            coreStatus.setText(VoicevoxRuntime.get(this).isCoreReady()
                    ? VoicevoxRuntime.get(this).diagnosticSummary()
                    : "未初期化（必要時に起動）");
        }
        if (currentVoice != null) {
            currentVoice.setText(option.label + "\nスタイルID=" + option.styleId
                    + "\nモデル=" + option.modelAsset
                    + "\nクレジット=" + option.credit);
        }
    }

    private TextView labeledBlock(LinearLayout root, String label) {
        TextView title = new TextView(this);
        title.setText(label);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(8), 0, dp(4));
        root.addView(title);
        TextView body = new TextView(this);
        body.setTextSize(14);
        body.setTextIsSelectable(true);
        body.setPadding(0, 0, 0, dp(8));
        root.addView(body);
        return body;
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

    private void applyCutoutPadding(View view, int pad) {
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            int left;
            int top;
            int right;
            int bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                left = bars.left;
                top = bars.top;
                right = bars.right;
                bottom = bars.bottom;
            } else {
                left = insets.getSystemWindowInsetLeft();
                top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight();
                bottom = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(pad + left, pad + top, pad + right, pad + bottom);
            if (Build.VERSION.SDK_INT >= 30) {
                return WindowInsets.CONSUMED;
            }
            return insets.consumeSystemWindowInsets();
        });
        view.requestApplyInsets();
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
