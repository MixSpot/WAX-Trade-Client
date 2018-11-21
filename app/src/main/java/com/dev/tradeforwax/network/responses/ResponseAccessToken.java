package com.dev.tradeforwax.network.responses;

public class ResponseAccessToken {
    public String access_token;
    public String token_type;
    public int expires_in;
    public String scope;
    public String refresh_token;

    public String error;
    public String error_description;
}
