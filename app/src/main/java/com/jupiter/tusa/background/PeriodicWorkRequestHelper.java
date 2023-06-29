package com.jupiter.tusa.background;


import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class PeriodicWorkRequestHelper {

    public static final String WorkName = "TUSA_MAIN";

    public static void requestMainWorker(Context context, boolean doOnStart) {
        WorkManager workManager = WorkManager.getInstance(context);
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data input = new Data.Builder()
                .putBoolean(TusaWorker.DoOnStartParamName, doOnStart)
                .build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                TusaWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setInputData(input)
                .addTag(WorkName)
                .build();

        workManager.enqueueUniquePeriodicWork(
                WorkName,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
        );
    }
}
