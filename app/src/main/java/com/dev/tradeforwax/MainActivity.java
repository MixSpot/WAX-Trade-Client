package com.dev.tradeforwax;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.dev.tradeforwax.models.RecentPartner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String ARG_API_KEY = "api_key";
    public static final String ARG_DEVICE_ID = "device_id";
    public static final String ARG_AUTH_REFRESH = "auth_refresh";
    public static final String ARG_AUTH_TOKEN = "auth_token";

    public static final String ARG_RECENT_PARTNERS = "recent_partners";

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Set<String> recentPartnersHash = mPreferences.getStringSet(ARG_RECENT_PARTNERS, null);

        final MainActivityViewModel viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        viewModel.setRecentPartners(recentPartnersHash);
        viewModel.getRecentPartners().observe(this, new Observer<List<RecentPartner>>() {
            @Override
            public void onChanged(List<RecentPartner> recentPartnerMap) {
                final Set<String> set = new HashSet<>();
                int i = 0;
                for(RecentPartner recentPartner : recentPartnerMap) {
                    recentPartner.order = i++;
                    set.add(ObjectSerializer.serialize(recentPartner));
                }

                mPreferences.edit().putStringSet(ARG_RECENT_PARTNERS, set).apply();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
