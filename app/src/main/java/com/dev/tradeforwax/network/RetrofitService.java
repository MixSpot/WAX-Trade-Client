package com.dev.tradeforwax.network;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class RetrofitService {

    private static APIServices INSTANCE;

    private static final String BASE_URL = "https://api-trade.opskins.com/";
    private static String mApiKey = null;
    private static String mToken = null;

    public static void setApiKey(String apiKey){
        mApiKey = apiKey;
    }
    public static void setToken(String token){
        mToken = token;
    }

    public static APIServices getInstance()
    {
        if (INSTANCE == null) {
            final OkHttpClient.Builder httpClient =
                    new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    final Request original = chain.request();
                    final Request.Builder requestBuilder = original.newBuilder();
                    if (mApiKey != null){
                        final HttpUrl originalHttpUrl = original.url().newBuilder()
                                .addQueryParameter("key", mApiKey).build();
                        requestBuilder.url(originalHttpUrl);
                    }
                    else if (mToken != null && original.header("Authorization") == null) {
                        requestBuilder.header("Authorization", "Bearer "+mToken);
                    }

                    return chain.proceed(requestBuilder.build());
                }
            });


            INSTANCE = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build())
                .build().create(APIServices.class);
        }
        return INSTANCE;
    }

}
