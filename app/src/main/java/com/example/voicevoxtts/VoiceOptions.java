package com.example.voicevoxtts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class VoiceOptions {
    static final class Option {
        final String label;
        final int styleId;
        final String modelAsset;
        final String credit;

        Option(String label, int styleId, String modelAsset, String credit) {
            this.label = label;
            this.styleId = styleId;
            this.modelAsset = modelAsset;
            this.credit = credit;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    static final List<Option> ALL = Collections.unmodifiableList(Arrays.asList(
            new Option("WhiteCUL / ノーマル", 23, "8.vvm", "VOICEVOX:WhiteCUL"),
            new Option("WhiteCUL / たのしい", 24, "8.vvm", "VOICEVOX:WhiteCUL"),
            new Option("WhiteCUL / かなしい", 25, "8.vvm", "VOICEVOX:WhiteCUL"),
            new Option("WhiteCUL / びえーん", 26, "8.vvm", "VOICEVOX:WhiteCUL"),
            new Option("四国めたん / ノーマル", 2, "0.vvm", "VOICEVOX:四国めたん"),
            new Option("四国めたん / あまあま", 0, "0.vvm", "VOICEVOX:四国めたん"),
            new Option("四国めたん / ツンツン", 6, "0.vvm", "VOICEVOX:四国めたん"),
            new Option("四国めたん / セクシー", 4, "0.vvm", "VOICEVOX:四国めたん")
    ));

    static Option byStyleId(int styleId) {
        for (Option option : ALL) {
            if (option.styleId == styleId) return option;
        }
        return ALL.get(0);
    }

    private VoiceOptions() {}
}
