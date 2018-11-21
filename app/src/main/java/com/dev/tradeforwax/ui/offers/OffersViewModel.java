package com.dev.tradeforwax.ui.offers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.ResponseOffers;

import java.util.List;

public class OffersViewModel extends ViewModel {
    private LiveData<ResponseOffers> offers;
    public int type;

    public LiveData<ResponseOffers> getOffers(String mSort, String type, List<Integer> filter, boolean isRefresh) {
        this.type = type.equals("received") ? 0 : 1;

        if (isRefresh || offers == null) {
            offers = Repository.getOffers(mSort, type, filter);
        }
        return offers;
    }
}
