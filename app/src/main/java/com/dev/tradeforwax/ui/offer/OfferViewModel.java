package com.dev.tradeforwax.ui.offer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.ResponseOffer;

public class OfferViewModel extends ViewModel {
    private MutableLiveData<ResponseOffer> offer;

    public LiveData<ResponseOffer> getOffer(int offerId, boolean isRefresh) {
        if (isRefresh || offer == null) {
            offer = Repository.getOffer(offerId);
        }
        return offer;
    }

    public void updateData(ResponseOffer responseOffer){
        offer.setValue(responseOffer);
    }
}
