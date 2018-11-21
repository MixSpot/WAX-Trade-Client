package com.dev.tradeforwax.ui.apikey;

import androidx.lifecycle.Observer;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.ResponseAccessToken;

import java.util.UUID;

import androidx.navigation.fragment.NavHostFragment;

import static com.dev.tradeforwax.MainActivity.ARG_AUTH_REFRESH;
import static com.dev.tradeforwax.MainActivity.ARG_AUTH_TOKEN;
import static com.dev.tradeforwax.MainActivity.ARG_DEVICE_ID;

public class ApiKeyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apikey, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String deviceID = preferences.getString(ARG_DEVICE_ID, null);
        if (deviceID == null) {
            deviceID = UUID.randomUUID().toString();
            preferences.edit().putString(ARG_DEVICE_ID, deviceID).apply();
        }

        final View group = view.findViewById(R.id.group);
        final View progress = view.findViewById(R.id.progressBar2);
        final View button = view.findViewById(R.id.oauth);

        final Uri uri = getActivity().getIntent().getData();
        if(savedInstanceState == null && uri != null && uri.getQueryParameter("code") != null){
            group.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            button.setClickable(false);
            button.setEnabled(false);

            final String state = uri.getQueryParameter("state");
            final String code = uri.getQueryParameter("code");

            final String clientID = getString(R.string.oauth_client_id);
            Repository.getToken(clientID, deviceID, code).observe(this, new Observer<ResponseAccessToken>() {
                @Override
                public void onChanged(@Nullable ResponseAccessToken responseAccessToken) {
                    if (responseAccessToken != null) {
                        preferences.edit()
                                .putString(ARG_AUTH_TOKEN, responseAccessToken.access_token)
                                .putString(ARG_AUTH_REFRESH, responseAccessToken.refresh_token)
                                .commit();
                        NavHostFragment.findNavController(ApiKeyFragment.this).navigateUp();
                    }
                    else{
                        group.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.VISIBLE);
                        button.setClickable(true);
                        button.setEnabled(true);
                    }
                }
            });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.isClickable()) {
                    final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ContextCompat.getColor(v.getContext(), R.color.primaryColor));
                    final CustomTabsIntent customTabsIntent = builder.build();

                    final Uri.Builder uri = new Uri.Builder();
                    uri.scheme("https")
                            .authority("oauth.opskins.com")
                            .appendPath("v1")
                            .appendPath("authorize")
                            .appendQueryParameter("client_id", getString(R.string.oauth_client_id))
                            .appendQueryParameter("state", "1234567890")
                            .appendQueryParameter("duration", "permanent")
                            .appendQueryParameter("mobile", "1")
                            .appendQueryParameter("response_type", "code")
                            .appendQueryParameter("scope", "identity_basic identity trades items manage_items"); //edit_account

                    customTabsIntent.launchUrl(v.getContext(), Uri.parse(uri.build().toString()));
                }
            }
        });
    }
}
