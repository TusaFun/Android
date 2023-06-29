package com.jupiter.tusa.data;

import com.google.common.util.concurrent.ListenableFuture;
import com.jupiter.tusa.Tusa;
import com.jupiter.tusa.TusaUserGrpc;
import com.jupiter.tusa.data.model.LoggedInUser;
import com.jupiter.tusa.grpc.TusaGrpc;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            TusaUserGrpc.TusaUserFutureStub stub = TusaGrpc.getInstance().getTusaUserFutureStub();
            Tusa.TusaTokenRequest request = Tusa.TusaTokenRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();
            //ListeningExecutorService executor = MoreExecutors.newDirectExecutorService();
            ListenableFuture<Tusa.TusaTokenReply> replyFuture = stub.tusaToken(request);
            Tusa.TusaTokenReply reply = replyFuture.get();
            String accessToken = reply.getAccessToken();
            String refreshToken = reply.getRefreshToken();
            long expirationTimeout = reply.getExpirationTimestamp();
            double expireMillisecondsIn = reply.getExpireMillisecondsIn();
            LoggedInUser user = new LoggedInUser(username, accessToken, refreshToken, expirationTimeout, expireMillisecondsIn);

            return new Result.Success<>(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public Result logout() {
        try {
            TusaUserGrpc.TusaUserFutureStub stub = TusaGrpc.getInstance().getTusaUserFutureStub();
            Tusa.RevokeTusaTokenRequest request = Tusa.RevokeTusaTokenRequest.newBuilder().build();
            Tusa.RevokeTusaTokenReply reply = stub.revokeTusaToken(request).get();
            return new Result.Success(null);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logout", e));
        }
    }
}