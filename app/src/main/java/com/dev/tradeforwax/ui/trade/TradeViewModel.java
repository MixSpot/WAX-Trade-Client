package com.dev.tradeforwax.ui.trade;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.ResponseApps;
import com.dev.tradeforwax.network.responses.ResponsePartnerInventory;
import com.dev.tradeforwax.network.responses.ResponseYourInventory;

public class TradeViewModel extends ViewModel {
    private LiveData<ResponseApps> apps;
    private LiveData<ResponseYourInventory> yourInventory;
    private LiveData<ResponsePartnerInventory> partnerInventory;

    public LiveData<ResponseApps> getApps(boolean isRefresh) {
        if (isRefresh || apps == null) {
            apps = Repository.getApps();
        }
        return apps;
    }

    public LiveData<ResponseYourInventory> getYourInventory(boolean isRefresh, int appId, int sort, String search) {
        if (isRefresh || yourInventory == null) {
            yourInventory = Repository.getYourInventory(appId, sort, search);
        }
        return yourInventory;
    }

    public LiveData<ResponsePartnerInventory> getPartnerInventory(boolean isRefresh, int uid, int appId, int sort, String search) {
        if (isRefresh || partnerInventory == null) {
            partnerInventory = Repository.getPartnerInventory(uid, appId, sort, search);
        }
        return partnerInventory;
    }
    public LiveData<ResponsePartnerInventory> getPartnerInventory(boolean isRefresh, String steamID, int appId, int sort, String search) {
        if (isRefresh || partnerInventory == null) {
            partnerInventory = Repository.getPartnerInventoryBySteamID(steamID, appId, sort, search);
        }
        return partnerInventory;
    }


}
