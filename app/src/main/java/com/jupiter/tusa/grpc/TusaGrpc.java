package com.jupiter.tusa.grpc;

import android.content.Context;
import android.content.SharedPreferences;

import com.jupiter.tusa.BuildConfig;
import com.jupiter.tusa.TusaUserGrpc;
import com.jupiter.tusa.background.TusaWorker;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

public class TusaGrpc {
    private static volatile TusaGrpc instance;

    private final ManagedChannel channel;

    private TusaGrpc() {
        channel = ManagedChannelBuilder.forAddress(BuildConfig.GRPC_HOST, BuildConfig.GRPC_PORT).build();
    }

    public static TusaGrpc getInstance() {
        if (instance == null) {
            instance = new TusaGrpc();
        }
        return instance;
    }

    public TusaUserGrpc.TusaUserFutureStub getTusaUserFutureStub() {
        return TusaUserGrpc.newFutureStub(channel);
    }

    public TusaUserGrpc.TusaUserFutureStub getTusaUserFutureStubWithAccessToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TusaWorker.SharedPreferencesName, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(TusaWorker.SharedPreferencesAccessTokenKey, "");
        TusaUserGrpc.TusaUserFutureStub stub = TusaUserGrpc.newFutureStub(channel);
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + accessToken);
        ClientInterceptor interceptor = MetadataUtils.newAttachHeadersInterceptor(headers);
        return stub.withInterceptors(interceptor);
    }
}
