package com.jupiter.tusa.background;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.jupiter.tusa.BuildConfig;
import com.jupiter.tusa.Tusa;
import com.jupiter.tusa.TusaUserGrpc;

import java.util.Date;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class TusaWorker extends Worker {

    public static final String DoOnStartParamName = "DO_ON_START";

    public static final String SharedPreferencesName = "TUSA_PREFERENCES";
    public static final String SharedPreferencesLastUpdateKey = "LAST_REFRESHED";
    public static final String SharedPreferencesAccessTokenKey = "ACCESS_TOKEN";
    public static final String SharedPreferencesRefreshTokenKey = "REFRESH_TOKEN";
    public static final String SharedPreferencesAccessTokenExpiresInMillisecondsKey = "ACCESS_TOKEN_EXP_IN";
    public static final String SharedPreferencesAccessTokenExpiresTimestampMillisecondsKey = "ACCESS_TOKEN_EXP_TIME";

    public TusaWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean doOnStart = getInputData().getBoolean(DoOnStartParamName, true);
        if(!doOnStart) {
            return Result.success();
        }
        ManagedChannel channel = ManagedChannelBuilder.forAddress(BuildConfig.GRPC_HOST, BuildConfig.GRPC_PORT).build();
        TusaUserGrpc.TusaUserFutureStub userStub = TusaUserGrpc.newFutureStub(channel);
        SharedPreferences privatePreferences = getApplicationContext().getSharedPreferences(
                SharedPreferencesName, Context.MODE_PRIVATE);
        String accessToken = privatePreferences.getString(TusaWorker.SharedPreferencesAccessTokenKey, "");
        String refreshToken = privatePreferences.getString(TusaWorker.SharedPreferencesRefreshTokenKey, "");

        Tusa.RefreshTokenRequest refreshTokenRequest = Tusa.RefreshTokenRequest.newBuilder()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
        ListenableFuture<Tusa.RefreshTokenReply> listenableRefreshTokenReply = userStub.refreshToken(refreshTokenRequest);

        SharedPreferences.Editor editor = privatePreferences.edit();
        try {
//            Tusa.RefreshTokenReply reply = listenableRefreshTokenReply.get();
//            String newAccessToken = reply.getAccessToken();
//            String newRefreshToken = reply.getRefreshToken();
//            double newExpireInMilliseconds = reply.getExpireMillisecondsIn();
//            long newExpireTimestamp = reply.getExpirationTimestamp();
//
//            Date date = new Date();
//            editor.putString(SharedPreferencesLastUpdateKey, date.toString());
//            editor.putString(SharedPreferencesAccessTokenKey, newAccessToken);
//            editor.putString(SharedPreferencesRefreshTokenKey, newRefreshToken);
//            editor.putLong(SharedPreferencesAccessTokenExpiresTimestampMillisecondsKey, newExpireTimestamp);
//            editor.putFloat(SharedPreferencesAccessTokenExpiresInMillisecondsKey, (float) newExpireInMilliseconds);
        } catch (Exception exception) {
            exception.printStackTrace();
            return Result.failure();
        } finally {
            editor.apply();
        }

        return Result.success();
    }
}
