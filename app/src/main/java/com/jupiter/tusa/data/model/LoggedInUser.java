package com.jupiter.tusa.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private final String username;
    private final String accessToken;
    private final String refreshToken;
    private final long expirationTimestamp;
    private final double expireInMilliseconds;

    public LoggedInUser(String username, String accessToken, String refreshToken, long expirationTimestamp,
     double expireInMilliseconds) {
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTimestamp = expirationTimestamp;
        this.expireInMilliseconds = expireInMilliseconds;
    }

    public String getUsername() {
        return username;
    }
    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }
    public double getExpireInMilliseconds() {
        return expireInMilliseconds;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public String getAccessToken() {
        return accessToken;
    }

}