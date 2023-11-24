package com.jupiter.tusa.utils;

import android.os.Handler;
import android.os.Looper;

public class RunOnMainUIThread {
    public static void post(Runnable runnable) {
        Handler mainUIHandler = new Handler(Looper.getMainLooper());
        mainUIHandler.post(runnable);
    }
}
