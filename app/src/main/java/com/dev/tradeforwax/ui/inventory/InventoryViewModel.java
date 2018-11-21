package com.dev.tradeforwax.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.ResponseApps;

public class InventoryViewModel extends ViewModel {
    private LiveData<ResponseApps> apps;

    public LiveData<ResponseApps> getApps(boolean isRefresh) {
        if (isRefresh || apps == null) {
            apps = Repository.getApps();
        }
        return apps;
    }
}
