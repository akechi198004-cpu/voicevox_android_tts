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
            new Option("WhiteCUL", 23, "8.vvm", "VOICEVOX:WhiteCUL"),
            new Option("四国めたん", 2, "0.vvm", "VOICEVOX:四国めたん"),
            new Option("玄野武宏", 11, "4.vvm", "VOICEVOX:玄野武宏"),
            new Option("No.7", 29, "6.vvm", "VOICEVOX:No.7")
    ));

    static Option byStyleId(int styleId) {
        for (Option option : ALL) {
            if (option.styleId == styleId) return option;
        }
        return ALL.get(0);
    }

    static Option fromVoiceName(String voiceName) {
        if (voiceName == null || !voiceName.startsWith("voicevox:")) return null;
        try {
            int styleId = Integer.parseInt(voiceName.substring("voicevox:".length()));
            for (Option option : ALL) {
                if (option.styleId == styleId) return option;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private VoiceOptions() {}
}
