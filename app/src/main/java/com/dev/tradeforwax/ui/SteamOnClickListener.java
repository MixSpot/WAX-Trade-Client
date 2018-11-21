package com.dev.tradeforwax.ui;


import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import android.view.View;

import com.dev.tradeforwax.R;

public class SteamOnClickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(v.getContext(), R.color.primaryColor));
        final CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(v.getContext(), Uri.parse("https://steamcommunity.com/profiles/"+v.getTag().toString()));
    }
}
