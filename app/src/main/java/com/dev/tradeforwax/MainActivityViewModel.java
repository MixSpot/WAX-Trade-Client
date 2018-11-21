package com.dev.tradeforwax;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dev.tradeforwax.models.RecentPartner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<List<RecentPartner>> mRecentPartnersData = new MutableLiveData<>();
    private final LinkedHashMap<String, RecentPartner> mRecentPartners = new LinkedHashMap<>();

    public LiveData<List<RecentPartner>> getRecentPartners(){
        return mRecentPartnersData;
    }
    public List<RecentPartner> getRecentPartnersList(){
        return new ArrayList<>(mRecentPartners.values());
    }

    public void setRecentPartners(Set<String> set){
        mRecentPartners.clear();
        if(set != null && !set.isEmpty())
        {
            final List<RecentPartner> sortList = new ArrayList<>();
            for (final String recentPartnerString : set) {
                RecentPartner recentPartner = (RecentPartner) ObjectSerializer.deserialize(recentPartnerString);
                if(recentPartner != null)
                    sortList.add(recentPartner);
            }
            Collections.sort(sortList);
            for (RecentPartner recentPartner : sortList)
                mRecentPartners.put(getKey(recentPartner), recentPartner);
        }
        refreshRecentPartner();
    }
    public void addRecentPartner(RecentPartner recentPartner){
        final String key = getKey(recentPartner);
        mRecentPartners.remove(key);
        mRecentPartners.put(key, recentPartner);

        if(mRecentPartners.size() > 10) {
            String lastKey = mRecentPartners.keySet().iterator().next();
            mRecentPartners.remove(lastKey);
        }

        refreshRecentPartner();
    }
    public void removeRecentPartner(RecentPartner recentPartner){
        final String key = getKey(recentPartner);
        mRecentPartners.remove(key);

        refreshRecentPartner();
    }

    private static String getKey(RecentPartner recentPartner){
        if (recentPartner.steam_id != null)
            return recentPartner.steam_id;

        return recentPartner.uid+"|"+recentPartner.token;
    }

    private void refreshRecentPartner(){
        mRecentPartnersData.setValue(new ArrayList<>(mRecentPartners.values()));
    }
}
