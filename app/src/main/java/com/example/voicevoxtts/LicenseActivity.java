package com.example.voicevoxtts;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.WindowInsets;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class LicenseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scroll = new ScrollView(this);
        TextView text = new TextView(this);
        int pad = Math.round(16 * getResources().getDisplayMetrics().density);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        text.setTextIsSelectable(true);
        text.setText(buildText());
        scroll.addView(text);
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
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
        scroll.requestApplyInsets();
        setContentView(scroll);
    }

    private String buildText() {
        return "第三者ソフトウェア・ライセンス\n\n"
                + "VOICEVOX\n\n"
                + "VOICEVOX Core 0.17.0\n"
                + "MITライセンス\n\n"
                + "VOICEVOX:WhiteCUL\n"
                + "VOICEVOX:四国めたん\n"
                + "VOICEVOX:玄野武宏\n"
                + "VOICEVOX:No.7\n\n"
                + "VOICEVOX ONNX Runtime 1.23.2\n"
                + "Open JTalk辞書 1.11\n"
                + "VVM 0.17.0 (0.vvm / 4.vvm / 6.vvm / 8.vvm)\n\n"
                + "注意：No.7 の商用利用は No.7 製作委員会への事前確認が必要です。\n\n"
                + "生成音声を利用する際は、各音声ライブラリの規約に従い、\n"
                + "VOICEVOX を利用したことがわかるクレジット表記が必要です。\n\n"
                + "--- VOICEVOX Core MIT ---\n\n"
                + readAsset("licenses/VOICEVOX_CORE_LICENSE.txt");
    }

    private String readAsset(String path) {
        try (InputStream in = getAssets().open(path);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return "（ライセンスファイルが見つかりません: " + path + "）";
        }
    }
}
