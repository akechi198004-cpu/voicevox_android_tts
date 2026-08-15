package com.example.voicevoxtts;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

final class DeviceInfo {
    static String summary(Context context) {
        ActivityManager.MemoryInfo mem = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) am.getMemoryInfo(mem);
        long totalRamMb = mem.totalMem / (1024 * 1024);
        long availRamMb = mem.availMem / (1024 * 1024);
        StringBuilder abis = new StringBuilder();
        for (String abi : Build.SUPPORTED_ABIS) {
            if (abis.length() > 0) abis.append(", ");
            abis.append(abi);
        }
        return "メーカー = " + Build.MANUFACTURER + "\n"
                + "モデル = " + Build.MODEL + "\n"
                + "デバイス = " + Build.DEVICE + "\n"
                + "Android = " + Build.VERSION.RELEASE + " / SDK " + Build.VERSION.SDK_INT + "\n"
                + "ABI = " + abis + "\n"
                + "RAM = " + totalRamMb + "MB（利用可能 " + availRamMb + "MB）\n"
                + "CPUコア数 = " + Runtime.getRuntime().availableProcessors();
    }

    private DeviceInfo() {}
}
