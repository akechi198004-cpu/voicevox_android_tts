package com.example.voicevoxtts;

import java.nio.charset.StandardCharsets;

final class WavPcm {
    final int sampleRate;
    final int channelCount;
    final int bitsPerSample;
    final int dataOffset;
    final int dataLength;

    private WavPcm(int sampleRate, int channelCount, int bitsPerSample, int dataOffset, int dataLength) {
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.bitsPerSample = bitsPerSample;
        this.dataOffset = dataOffset;
        this.dataLength = dataLength;
    }

    static WavPcm parse(byte[] wav) {
        if (wav.length < 44 || !ascii(wav, 0, 4).equals("RIFF") || !ascii(wav, 8, 4).equals("WAVE")) {
            throw new IllegalArgumentException("Not a RIFF/WAVE file");
        }

        int sampleRate = 0;
        int channels = 0;
        int bits = 0;
        int format = 0;
        int dataOffset = -1;
        int dataLength = -1;

        int p = 12;
        while (p + 8 <= wav.length) {
            String id = ascii(wav, p, 4);
            int size = le32(wav, p + 4);
            int body = p + 8;
            if (size < 0 || body + size > wav.length) {
                throw new IllegalArgumentException("Broken WAV chunk: " + id);
            }
            if ("fmt ".equals(id)) {
                if (size < 16) throw new IllegalArgumentException("Short fmt chunk");
                format = le16(wav, body);
                channels = le16(wav, body + 2);
                sampleRate = le32(wav, body + 4);
                bits = le16(wav, body + 14);
            } else if ("data".equals(id)) {
                dataOffset = body;
                dataLength = size;
                break;
            }
            p = body + size + (size & 1);
        }

        if (format != 1) throw new IllegalArgumentException("Only PCM WAV is supported, format=" + format);
        if (channels < 1 || channels > 2) throw new IllegalArgumentException("Unsupported channels=" + channels);
        if (bits != 16) throw new IllegalArgumentException("Only PCM16 is supported, bits=" + bits);
        if (sampleRate <= 0 || dataOffset < 0 || dataLength < 0) {
            throw new IllegalArgumentException("Missing WAV fmt/data chunk");
        }
        return new WavPcm(sampleRate, channels, bits, dataOffset, dataLength);
    }

    private static int le16(byte[] b, int o) {
        return (b[o] & 0xff) | ((b[o + 1] & 0xff) << 8);
    }

    private static int le32(byte[] b, int o) {
        return (b[o] & 0xff)
                | ((b[o + 1] & 0xff) << 8)
                | ((b[o + 2] & 0xff) << 16)
                | ((b[o + 3] & 0xff) << 24);
    }

    private static String ascii(byte[] b, int o, int n) {
        return new String(b, o, n, StandardCharsets.US_ASCII);
    }
}
