package com.dev.tradeforwax.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.ResponseOffersSummary;
import com.dev.tradeforwax.network.responses.ResponseUser;

public class MainViewModel extends ViewModel {
    private LiveData<ResponseUser> user;
    private LiveData<ResponseOffersSummary> offers;

    public LiveData<ResponseUser> getUser(boolean isRefresh) {
        if (isRefresh || user == null) {
            user = Repository.getUser();
        }
        return user;
    }

    public LiveData<ResponseOffersSummary> getOffers(boolean isRefresh) {
        if (isRefresh || offers == null) {
            offers = Repository.getOffersSummary();
        }
        return offers;
    }
}
