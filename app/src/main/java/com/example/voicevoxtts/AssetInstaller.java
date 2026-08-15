package com.example.voicevoxtts;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class AssetInstaller {
    private final Context context;
    private final AssetManager assets;
    private final File root;

    AssetInstaller(Context context) {
        this.context = context.getApplicationContext();
        this.assets = this.context.getAssets();
        this.root = new File(this.context.getFilesDir(), "voicevox_runtime");
    }

    File installDict() throws IOException {
        File dst = new File(root, "dict/open_jtalk_dic_utf_8-1.11");
        copyAssetTree("voicevox/dict/open_jtalk_dic_utf_8-1.11", dst);
        return dst;
    }

    File installModel(String fileName) throws IOException {
        File dst = new File(root, "models/" + fileName);
        copyAssetFileIfNeeded("voicevox/models/" + fileName, dst);
        return dst;
    }

    private void copyAssetTree(String assetPath, File dst) throws IOException {
        String[] children = assets.list(assetPath);
        if (children == null) {
            throw new IOException("Cannot list asset path: " + assetPath);
        }
        if (children.length == 0) {
            copyAssetFileIfNeeded(assetPath, dst);
            return;
        }
        if (!dst.exists() && !dst.mkdirs()) {
            throw new IOException("Cannot create directory: " + dst);
        }
        for (String child : children) {
            copyAssetTree(assetPath + "/" + child, new File(dst, child));
        }
    }

    private void copyAssetFileIfNeeded(String assetPath, File dst) throws IOException {
        if (dst.exists() && dst.length() > 0) return;
        File parent = dst.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create directory: " + parent);
        }
        File tmp = new File(dst.getAbsolutePath() + ".tmp");
        try (InputStream in = assets.open(assetPath);
             OutputStream out = new FileOutputStream(tmp)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
        }
        if (!tmp.renameTo(dst)) {
            // Some filesystems do not replace atomically. Fall back to a second copy.
            if (dst.exists() && !dst.delete()) {
                throw new IOException("Cannot replace: " + dst);
            }
            if (!tmp.renameTo(dst)) {
                throw new IOException("Cannot move " + tmp + " -> " + dst);
            }
        }
    }
}
