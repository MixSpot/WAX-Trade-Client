package com.dev.tradeforwax.network;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import com.dev.tradeforwax.models.Item;
import com.dev.tradeforwax.models.OpskinsInventoryItem;
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
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {

    public static LiveData<ResponseAccessToken> getToken(String clientID, String deviceID, String code)
    {
        final Map<String, String> fields = new HashMap<>();
        fields.put("grant_type", "authorization_code");
        fields.put("code", code);
        final String base64 = new String(Base64.encode((clientID+":"+deviceID).getBytes(), Base64.NO_WRAP));

        final MutableLiveData<ResponseAccessToken> data = new MutableLiveData<>();
        RetrofitService.getInstance().accessToken("Basic "+base64, fields).enqueue(new Callback<ResponseAccessToken>() {
            @Override
            public void onResponse(Call<ResponseAccessToken> call, Response<ResponseAccessToken> response) {
                Log.d("csss", response.toString());

                if (response.code() == 401) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseAccessToken.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseAccessToken> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseAccessToken> refreshToken(String clientID, String deviceID, String refreshToken)
    {
        final Map<String, String> fields = new HashMap<>();
        fields.put("grant_type", "refresh_token");
        fields.put("refresh_token", refreshToken);
        final String base64 = new String(Base64.encode((clientID+":"+deviceID).getBytes(), Base64.NO_WRAP));

        final MutableLiveData<ResponseAccessToken> data = new MutableLiveData<>();
        RetrofitService.getInstance().accessToken("Basic "+base64, fields).enqueue(new Callback<ResponseAccessToken>() {
            @Override
            public void onResponse(Call<ResponseAccessToken> call, Response<ResponseAccessToken> response) {
                Log.d("csss", response.toString());

                if (response.code() == 401 || response.code() == 400) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseAccessToken.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseAccessToken> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }

    public static LiveData<ResponseUser> getUser()
    {
        final MutableLiveData<ResponseUser> data = new MutableLiveData<>();
        RetrofitService.getInstance().getUser().enqueue(new Callback<ResponseUser>() {
            @Override
            public void onResponse(Call<ResponseUser> call, Response<ResponseUser> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseUser.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseUser> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseApps> getApps()
    {
        final MutableLiveData<ResponseApps> data = new MutableLiveData<>();
        RetrofitService.getInstance().getApps().enqueue(new Callback<ResponseApps>() {
            @Override
            public void onResponse(Call<ResponseApps> call, Response<ResponseApps> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseApps.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseApps> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseTradeURL> getTradeURL()
    {
        final MutableLiveData<ResponseTradeURL> data = new MutableLiveData<>();
        RetrofitService.getInstance().getTradeURL().enqueue(new Callback<ResponseTradeURL>() {
            @Override
            public void onResponse(Call<ResponseTradeURL> call, Response<ResponseTradeURL> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseTradeURL.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseTradeURL> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }

    public static LiveData<ResponseYourInventory> getYourInventory(int appId, int sort, String search)
    {
        final MutableLiveData<ResponseYourInventory> data = new MutableLiveData<>();
        RetrofitService.getInstance().getYourInventory(appId, sort, search).enqueue(new Callback<ResponseYourInventory>() {
            @Override
            public void onResponse(Call<ResponseYourInventory> call, Response<ResponseYourInventory> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseYourInventory.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseYourInventory> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponsePartnerInventory> getPartnerInventory(int uid, int appId, int sort, String search)
    {
        final MutableLiveData<ResponsePartnerInventory> data = new MutableLiveData<>();
        RetrofitService.getInstance().getPartnerInventory(uid, appId, sort, search).enqueue(new Callback<ResponsePartnerInventory>() {
            @Override
            public void onResponse(Call<ResponsePartnerInventory> call, Response<ResponsePartnerInventory> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponsePartnerInventory.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponsePartnerInventory> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponsePartnerInventory> getPartnerInventoryBySteamID(String steamID, int appId, int sort, String search)
    {
        final MutableLiveData<ResponsePartnerInventory> data = new MutableLiveData<>();
        RetrofitService.getInstance().getPartnerInventoryBySteamID(steamID, appId, sort, search).enqueue(new Callback<ResponsePartnerInventory>() {
            @Override
            public void onResponse(Call<ResponsePartnerInventory> call, Response<ResponsePartnerInventory> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponsePartnerInventory.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponsePartnerInventory> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseOffersSummary> getOffersSummary()
    {
        final MutableLiveData<ResponseOffersSummary> data = new MutableLiveData<>();
        RetrofitService.getInstance().getOffersSummary().enqueue(new Callback<ResponseOffersSummary>() {
            @Override
            public void onResponse(Call<ResponseOffersSummary> call, Response<ResponseOffersSummary> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseOffersSummary.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseOffersSummary> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static MutableLiveData<ResponseOffer> getOffer(int offerId)
    {
        final MutableLiveData<ResponseOffer> data = new MutableLiveData<>();
        RetrofitService.getInstance().getOffer(offerId).enqueue(new Callback<ResponseOffer>() {
            @Override
            public void onResponse(Call<ResponseOffer> call, Response<ResponseOffer> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseOffer.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseOffer> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseOffers> getOffers(String mSort, String type, List<Integer> filter)
    {
        final String filterS;
        if(filter.isEmpty())
            filterS = null;
        else {
            final StringBuilder strbul = new StringBuilder();
            final Iterator<Integer> iter = filter.iterator();
            while (iter.hasNext()) {
                strbul.append(iter.next());
                if (iter.hasNext())
                    strbul.append(",");
            }
            filterS = strbul.toString();
        }

        final MutableLiveData<ResponseOffers> data = new MutableLiveData<>();
        RetrofitService.getInstance().getOffers(null, type, mSort,  filterS).enqueue(new Callback<ResponseOffers>() {
            @Override
            public void onResponse(Call<ResponseOffers> call, Response<ResponseOffers> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseOffers.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseOffers> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }



    public static LiveData<ResponseOffer> sendOffer(int uid, String token, String message, SparseArray<Item> yourItemsArray, SparseArray<Item> partnerItemsArray, String twoFA)
    {
        final StringBuilder yourItems = new StringBuilder();
        for(int i = 0; i < yourItemsArray.size(); i++) {
            int key = yourItemsArray.keyAt(i);
            if(yourItems.length() != 0) yourItems.append(',');
            yourItems.append(key);
        }
        final StringBuilder partnerItems = new StringBuilder();
        for(int i = 0; i < partnerItemsArray.size(); i++) {
            int key = partnerItemsArray.keyAt(i);
            if(partnerItems.length() != 0) partnerItems.append(',');
            partnerItems.append(key);
        }

        final MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(uid == 0)
            requestBody.addFormDataPart("steam_id", token);
        else
            requestBody.addFormDataPart("uid", String.valueOf(uid))
                    .addFormDataPart("token", token);

        Log.d("test", String.valueOf(uid));
        Log.d("test", token);

        requestBody.addFormDataPart("items_to_send", yourItems.toString())
                .addFormDataPart("items_to_receive", partnerItems.toString())
                .addFormDataPart("message", message)
                .addFormDataPart("twofactor_code", twoFA);

        final MutableLiveData<ResponseOffer> data = new MutableLiveData<>();
        final Callback<ResponseOffer> callback = new Callback<ResponseOffer>() {
            @Override
            public void onResponse(Call<ResponseOffer> call, Response<ResponseOffer> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseOffer.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseOffer> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        };

        if (uid == 0)
            RetrofitService.getInstance().sendOfferToSteamID(requestBody.build()).enqueue(callback);
        else
            RetrofitService.getInstance().sendOffer(requestBody.build()).enqueue(callback);

        return data;
    }
    public static LiveData<ResponseOffer> acceptOffer(int offerId, String twoFA)
    {
        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("offer_id", String.valueOf(offerId))
                .addFormDataPart("twofactor_code", twoFA)
                .build();

        final MutableLiveData<ResponseOffer> data = new MutableLiveData<>();
        RetrofitService.getInstance().acceptOffer(requestBody).enqueue(new Callback<ResponseOffer>() {
            @Override
            public void onResponse(Call<ResponseOffer> call, Response<ResponseOffer> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseOffer.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseOffer> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseOffer> cancelOffer(int offerId)
    {
        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("offer_id", String.valueOf(offerId))
                .build();

        final MutableLiveData<ResponseOffer> data = new MutableLiveData<>();
        RetrofitService.getInstance().cancelOffer(requestBody).enqueue(new Callback<ResponseOffer>() {
            @Override
            public void onResponse(Call<ResponseOffer> call, Response<ResponseOffer> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseOffer.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseOffer> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseReport> sendReport(String message, int reportType, int offerId)
    {
        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", message)
                .addFormDataPart("report_type", String.valueOf(reportType))
                .addFormDataPart("offer_id", String.valueOf(offerId))
                .build();

        final MutableLiveData<ResponseReport> data = new MutableLiveData<>();
        RetrofitService.getInstance().sendReport(requestBody).enqueue(new Callback<ResponseReport>() {
            @Override
            public void onResponse(Call<ResponseReport> call, Response<ResponseReport> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseReport.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ResponseReport> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<com.dev.tradeforwax.network.responses.Response> withdrawToOpskins(String items)
    {
        final MutableLiveData<com.dev.tradeforwax.network.responses.Response> data = new MutableLiveData<>();
        RetrofitService.getInstance().withdrawToOpskins(items).enqueue(new Callback<com.dev.tradeforwax.network.responses.Response>() {
            @Override
            public void onResponse(Call<com.dev.tradeforwax.network.responses.Response> call, Response<com.dev.tradeforwax.network.responses.Response> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), com.dev.tradeforwax.network.responses.Response.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<com.dev.tradeforwax.network.responses.Response> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<com.dev.tradeforwax.network.responses.Response> importFromOpskins(String items)
    {
        final MutableLiveData<com.dev.tradeforwax.network.responses.Response> data = new MutableLiveData<>();
        RetrofitService.getInstance().transferToTradeSite(items).enqueue(new Callback<com.dev.tradeforwax.network.responses.Response>() {
            @Override
            public void onResponse(Call<com.dev.tradeforwax.network.responses.Response> call, Response<com.dev.tradeforwax.network.responses.Response> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), com.dev.tradeforwax.network.responses.Response.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else
                    data.setValue(response.body());
            }
            @Override
            public void onFailure(Call<com.dev.tradeforwax.network.responses.Response> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
    public static LiveData<ResponseOpskinsInventory> getOpskinsInventory(final int appid)
    {
        final MutableLiveData<ResponseOpskinsInventory> data = new MutableLiveData<>();
        RetrofitService.getInstance().getOpskinsInventory().enqueue(new Callback<ResponseOpskinsInventory>() {
            @Override
            public void onResponse(Call<ResponseOpskinsInventory> call, Response<ResponseOpskinsInventory> response) {
                Log.d("csss", response.toString());

                if (!response.isSuccessful()) {
                    try {
                        data.setValue(new GsonBuilder().create().fromJson(response.errorBody().string(), ResponseOpskinsInventory.class));
                    } catch (Exception e) {
                        data.setValue(null);
                    }
                }
                else {
                    final ResponseOpskinsInventory responseOpskinsInventory = response.body();
                    if (responseOpskinsInventory != null && responseOpskinsInventory.response.items.size() > 0)
                    {
                        final List<OpskinsInventoryItem> filteredList = new ArrayList<>();
                        for (OpskinsInventoryItem item: responseOpskinsInventory.response.items) {
                            if(item.appid == appid)
                                filteredList.add(item);
                        }
                        responseOpskinsInventory.response.items = filteredList;
                    }
                    data.setValue(responseOpskinsInventory);
                }
            }
            @Override
            public void onFailure(Call<ResponseOpskinsInventory> call, Throwable t) {
                t.printStackTrace();
                data.setValue(null);
            }
        });
        return data;
    }
}
