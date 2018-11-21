package com.dev.tradeforwax.ui.import_opskins;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.ResponseOpskinsInventory;

public class ImportOpskinsViewModel extends ViewModel {
    private LiveData<ResponseOpskinsInventory> inventory;

    public LiveData<ResponseOpskinsInventory> getInventory(int mCurrentApp, boolean isRefresh) {
        if (isRefresh || inventory == null) {
            inventory = Repository.getOpskinsInventory(mCurrentApp);
        }
        return inventory;
    }


}
