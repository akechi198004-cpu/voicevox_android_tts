package com.example.voicevoxtts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Product whitelist. VVM metadata is matched by speaker + style name;
 * only approved, peaceful styles are exposed. New official speakers
 * do not appear until they are added here.
 */
final class ApprovedVoices {
    static final String GROUP_CHARACTER = "キャラクター音声";
    static final String GROUP_NEMO = "VOICEVOX Nemo";
    static final String STYLE_NORMAL = "ノーマル";

    /**
     * ずんだもん is in 0.vvm and commercially usable, but the character
     * tone is strong. Flip this to hide it from UI and system voices.
     */
    static final boolean SHOW_ZUNDAMON = true;

    static final class Option {
        final String label;
        final int styleId;
        final String modelAsset;
        final String credit;
        final String group;
        final String speakerName;
        final String styleName;
        final boolean visible;

        Option(String label, int styleId, String modelAsset, String credit,
               String group, String speakerName, String styleName, boolean visible) {
            this.label = label;
            this.styleId = styleId;
            this.modelAsset = modelAsset;
            this.credit = credit;
            this.group = group;
            this.speakerName = speakerName;
            this.styleName = styleName;
            this.visible = visible;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final List<Option> APPROVED = Collections.unmodifiableList(Arrays.asList(
            character("WhiteCUL", 23, "8.vvm", "VOICEVOX:WhiteCUL", "WhiteCUL", true),
            character("四国めたん", 2, "0.vvm", "VOICEVOX:四国めたん", "四国めたん", true),
            character("ずんだもん", 3, "0.vvm", "VOICEVOX:ずんだもん", "ずんだもん", SHOW_ZUNDAMON),
            character("春日部つむぎ", 8, "0.vvm", "VOICEVOX:春日部つむぎ", "春日部つむぎ", true),
            character("雨晴はう", 10, "0.vvm", "VOICEVOX:雨晴はう", "雨晴はう", true),
            character("玄野武宏", 11, "4.vvm", "VOICEVOX:玄野武宏", "玄野武宏", true),
            character("剣崎雌雄", 21, "4.vvm", "VOICEVOX:剣崎雌雄", "剣崎雌雄", true),
            nemo("女声1", 10005),
            nemo("女声2", 10007),
            nemo("女声3", 10004),
            nemo("女声4", 10003),
            nemo("女声5", 10008),
            nemo("女声6", 10006),
            nemo("男声1", 10001),
            nemo("男声2", 10000),
            nemo("男声3", 10002)
    ));

    static List<Option> all() {
        return APPROVED;
    }

    static List<Option> visible() {
        List<Option> out = new ArrayList<>();
        for (Option option : APPROVED) {
            if (option.visible) out.add(option);
        }
        return Collections.unmodifiableList(out);
    }

    static Option byStyleId(int styleId) {
        for (Option option : APPROVED) {
            if (option.styleId == styleId) return option;
        }
        return APPROVED.get(0);
    }

    static Option fromVoiceName(String voiceName) {
        if (voiceName == null || !voiceName.startsWith("voicevox:")) return null;
        try {
            int styleId = Integer.parseInt(voiceName.substring("voicevox:".length()));
            for (Option option : APPROVED) {
                if (option.visible && option.styleId == styleId) return option;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    static List<String> credits() {
        Set<String> credits = new LinkedHashSet<>();
        credits.add("VOICEVOX");
        credits.add("VOICEVOX Core 0.17.0（MIT）");
        for (Option option : APPROVED) {
            credits.add(option.credit);
        }
        return new ArrayList<>(credits);
    }

    private static Option character(String label, int styleId, String model,
                                    String credit, String speakerName, boolean visible) {
        return new Option(label, styleId, model, credit, GROUP_CHARACTER,
                speakerName, STYLE_NORMAL, visible);
    }

    private static Option nemo(String speakerName, int styleId) {
        return new Option(speakerName, styleId, "n0.vvm", "VOICEVOX Nemo",
                GROUP_NEMO, speakerName, STYLE_NORMAL, true);
    }

    private ApprovedVoices() {}
}
