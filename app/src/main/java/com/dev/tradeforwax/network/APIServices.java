package com.dev.tradeforwax.network;

import com.dev.tradeforwax.network.responses.Response;
import com.dev.tradeforwax.network.responses.ResponseAccessToken;
import com.dev.tradeforwax.network.responses.ResponseApps;
import com.dev.tradeforwax.network.responses.ResponseOffer;
import com.dev.tradeforwax.network.responses.ResponseOffers;
import com.dev.tradeforwax.network.responses.ResponseOffersSummary;
import com.dev.tradeforwax.network.responses.ResponseOpskinsInventory;
import com.dev.tradeforwax.network.responses.ResponseReport;
import com.dev.tradeforwax.network.responses.ResponseYourInventory;
import com.dev.tradeforwax.network.responses.ResponseTradeURL;
import com.dev.tradeforwax.network.responses.ResponseUser;
import com.dev.tradeforwax.network.responses.ResponsePartnerInventory;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIServices {
    @FormUrlEncoded
    @POST("https://oauth.opskins.com/v1/access_token")
    Call<ResponseAccessToken> accessToken(@Header("Authorization") String auth, @FieldMap() Map<String, String> fields);


    @GET("/IUser/GetProfile/v1/")
    Call<ResponseUser> getUser();
    @GET("/ITrade/GetTradeURL/v1/")
    Call<ResponseTradeURL> getTradeURL();

    @GET("/ITrade/GetApps/v1/")
    Call<ResponseApps> getApps();

    @GET("/IUser/GetInventory/v1/")
    Call<ResponseYourInventory> getYourInventory(@Query("app_id") int appId, @Query("sort") int sort, @Query("search") String search);

    @GET("/ITrade/GetUserInventory/v1/")
    Call<ResponsePartnerInventory> getPartnerInventory(@Query("uid") int uid, @Query("app_id") int appId, @Query("sort") int sort, @Query("search") String search);
    @GET("/ITrade/GetUserInventoryFromSteamId/v1/")
    Call<ResponsePartnerInventory> getPartnerInventoryBySteamID(@Query("steam_id") String steamID, @Query("app_id") int appId, @Query("sort") int sort, @Query("search") String search);

    @GET("/ITrade/GetOffersSummary/v1/")
    Call<ResponseOffersSummary> getOffersSummary();

    @POST("/ITrade/SendOffer/v1/")
    Call<ResponseOffer> sendOffer(@Body RequestBody body);
    @POST("/ITrade/SendOfferToSteamId/v1/")
    Call<ResponseOffer> sendOfferToSteamID(@Body RequestBody body);

    @POST("/ITrade/AcceptOffer/v1/")
    Call<ResponseOffer> acceptOffer(@Body RequestBody body);
    @POST("/ITrade/CancelOffer/v1/")
    Call<ResponseOffer> cancelOffer(@Body RequestBody body);

    @GET("/ITrade/GetOffer/v1/")
    Call<ResponseOffer> getOffer(@Query("offer_id") int offerId);
    @GET("/ITrade/GetOffers/v1/")
    Call<ResponseOffers> getOffers(@Query("state") String state, @Query("type") String type, @Query("sort") String sort, @Query("state") String filter);

    @POST("/IUser/UserReports/v1")
    Call<ResponseReport> sendReport(@Body RequestBody body);

    @FormUrlEncoded
    @POST("/IItem/WithdrawToOpskins/v1/")
    Call<Response> withdrawToOpskins(@Field("item_id") String items);

    @FormUrlEncoded
    @POST("https://api.opskins.com/IInventory/TransferToTradeSite/v1/")
    Call<Response> transferToTradeSite(@Field("items") String items);
    @GET("https://api.opskins.com/IInventory/GetInventory/v2/")
    Call<ResponseOpskinsInventory> getOpskinsInventory();
}
