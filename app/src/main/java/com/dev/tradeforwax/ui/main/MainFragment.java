package com.dev.tradeforwax.ui.main;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.MainActivityViewModel;
import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.RecentPartner;
import com.dev.tradeforwax.models.User;
import com.dev.tradeforwax.network.RetrofitService;
import com.dev.tradeforwax.network.StatusCode;
import com.dev.tradeforwax.network.responses.ResponseAccessToken;
import com.dev.tradeforwax.network.responses.ResponseOffersSummary;
import com.dev.tradeforwax.network.responses.ResponseTradeURL;
import com.dev.tradeforwax.network.responses.ResponseUser;
import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.ui.trade.TradeFragment;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import static com.dev.tradeforwax.MainActivity.ARG_API_KEY;
import static com.dev.tradeforwax.MainActivity.ARG_AUTH_REFRESH;
import static com.dev.tradeforwax.MainActivity.ARG_AUTH_TOKEN;
import static com.dev.tradeforwax.MainActivity.ARG_DEVICE_ID;


public class MainFragment extends Fragment {

    private String mApiKey = null;
    private String mRefreshToken = null;
    private String mDeviceID = null;

    private String mTradeUrl = null;
    private View mLayout;
    private User mUser = null;

    private View progressBarUserView;
    private TextView userNameView;
    private ImageView userAvatarView;

    private View warning2FACard;
    private View enable2FAButton;

    private TextView mCountSend;
    private TextView mCountReceived;
    private View mProgressBarOffers;
    private View mOffersCounter;

    private RecentPartnersAdapter recentPartnersAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        mApiKey = preferences.getString(ARG_API_KEY, null);
        if (mApiKey != null) RetrofitService.setApiKey(mApiKey);

        mRefreshToken = preferences.getString(ARG_AUTH_REFRESH, null);
        mDeviceID = preferences.getString(ARG_DEVICE_ID, null);

        if(mApiKey == null && mRefreshToken == null)
            NavHostFragment.findNavController(MainFragment.this).navigate(
                    R.id.apiKeyFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
            );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));

        mLayout = view.findViewById(R.id.layout);

        userNameView = view.findViewById(R.id.name);
        userAvatarView = view.findViewById(R.id.avatar);
        progressBarUserView = view.findViewById(R.id.progressBarUser);

        warning2FACard = view.findViewById(R.id.warning2FACard);
        enable2FAButton = view.findViewById(R.id.enable2FAButton);

        final View emptyView = view.findViewById(R.id.emptyView);
        final RecyclerView mRecycleView = view.findViewById(R.id.recycleView);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycleView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        final List<RecentPartner> recentPartners = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class).getRecentPartnersList();
        emptyView.setVisibility(recentPartners.isEmpty() ? View.VISIBLE : View.GONE);

        recentPartnersAdapter = new RecentPartnersAdapter(getContext(), recentPartners);
        mRecycleView.setAdapter(recentPartnersAdapter);

        if (mRefreshToken != null)
            refreshToken();
        else if(mApiKey != null)
            loadUser(false);

        view.findViewById(R.id.viewInventory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null)
                    NavHostFragment.findNavController(MainFragment.this).navigate(R.id.inventoryFragment);
            }
        });

        mCountSend = view.findViewById(R.id.sendCounter);
        mCountReceived = view.findViewById(R.id.receivedCount);
        mProgressBarOffers = view.findViewById(R.id.progressBarOffers);
        mOffersCounter = view.findViewById(R.id.offersCounters);
        view.findViewById(R.id.refreshOffers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null && mProgressBarOffers.getVisibility() == View.GONE) {
                    mOffersCounter.setVisibility(View.INVISIBLE);
                    mProgressBarOffers.setVisibility(View.VISIBLE);
                    refreshOffers(true);
                }
            }
        });
        view.findViewById(R.id.viewAllOffers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null)
                    NavHostFragment.findNavController(MainFragment.this).navigate(R.id.offersFragment);
            }
        });

        final EditText tradeUrlView = view.findViewById(R.id.tradeUrl);
        final View startTradeButton = view.findViewById(R.id.startTrade);

        tradeUrlView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            startTradeButton.performClick();
                            return true;
                    }
                return false;
            }
        });

        view.findViewById(R.id.startTrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null) {
                    final String inputText = tradeUrlView.getText().toString();
                    final Matcher matcherTradeURL = Pattern.compile(getString(R.string.pattern_trade_url)).matcher(inputText);
                    final Matcher matcherSteamID = Pattern.compile(getString(R.string.pattern_steam_id)).matcher(inputText);
                    if (matcherTradeURL.find()) {
                        final int uid = Integer.parseInt(matcherTradeURL.group(1));
                        final String token = matcherTradeURL.group(2);

                        if (mUser.id == uid)
                            Snackbar.make(mLayout, R.string.you_cant_trade_with_yourself, Snackbar.LENGTH_SHORT).show();
                        else {
                            final Bundle bundle = new Bundle();
                            bundle.putInt(TradeFragment.ARG_UID, uid);
                            bundle.putString(TradeFragment.ARG_TOKEN, token);
                            tradeUrlView.setText("");
                            NavHostFragment.findNavController(MainFragment.this).navigate(R.id.tradeFragment, bundle);
                        }
                    } else if (matcherSteamID.find()) {
                        final String steamID = matcherSteamID.group(1);
                        if (steamID.equals(mUser.steam_id))
                            Snackbar.make(mLayout, R.string.you_cant_trade_with_yourself, Snackbar.LENGTH_SHORT).show();
                        else {
                            final Bundle bundle = new Bundle();
                            bundle.putString(TradeFragment.ARG_STEAM_ID, steamID);
                            tradeUrlView.setText("");
                            NavHostFragment.findNavController(MainFragment.this).navigate(R.id.tradeFragment, bundle);
                        }
                    }
                    else
                        Snackbar.make(mLayout, R.string.incorrect_trade_url, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUser(boolean isRefresh){
        ViewModelProviders.of(MainFragment.this).get(MainViewModel.class)
            .getUser(isRefresh).observe(this, new Observer<ResponseUser>() {
                @Override
                public void onChanged(@Nullable ResponseUser responseUser) {
                    progressBarUserView.setVisibility(View.GONE);

                    if(responseUser != null) {
                        Log.d("user", "status: " + responseUser.status);
                        if(responseUser.message != null)
                            Log.d("user", "message: " + responseUser.message);
                    }
                    if(responseUser == null){
                        Snackbar.make(mLayout, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadUser(true);
                            }
                        }).show();
                    }
                    else if(responseUser.status == StatusCode.OK) {
                        mUser = responseUser.response.user;
                        userNameView.setText(mUser.display_name);
                        if(mUser.avatar != null && mUser.getAvatar() != null)
                            Picasso.get().load(mUser.getAvatar()).into(userAvatarView);
                        else
                            userAvatarView.setImageResource(R.drawable.opskins_logo);

                        if(!mUser.twofactor_enabled)
                        {
                            warning2FACard.setVisibility(View.VISIBLE);
                            enable2FAButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                    builder.setToolbarColor(ContextCompat.getColor(v.getContext(), R.color.primaryColor));
                                    final CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(v.getContext(), Uri.parse("https://opskins.com/?loc=store_account#collapseSec"));
                                }
                            });
                        }
                        else
                            warning2FACard.setVisibility(View.GONE);

                        if(mApiKey != null)
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                                    .putString("api_key_prev", mApiKey+"|"+mUser.display_name).apply();

                        Repository.getTradeURL().observe(MainFragment.this, new Observer<ResponseTradeURL>() {
                            @Override
                            public void onChanged(@Nullable ResponseTradeURL responseTradeURL) {
                                if(responseTradeURL != null && responseTradeURL.status == StatusCode.OK)
                                    mTradeUrl = responseTradeURL.response.short_url;
                            }
                        });

                        refreshOffers(false);

                        recentPartnersAdapter.setActive();
                    }
                    else
                        NavHostFragment.findNavController(MainFragment.this).navigate(R.id.apiKeyFragment);
                }
            });
    }

    public void refreshOffers(boolean isRefresh){
        ViewModelProviders.of(MainFragment.this).get(MainViewModel.class).getOffers(isRefresh)
            .observe(this, new Observer<ResponseOffersSummary>() {
                @Override
                public void onChanged(@Nullable ResponseOffersSummary responseOffersSummary) {
                    mProgressBarOffers.setVisibility(View.GONE);
                    mOffersCounter.setVisibility(View.VISIBLE);
                    if(responseOffersSummary != null && responseOffersSummary.status == StatusCode.OK) {
                        mCountSend.setText(Integer.toString(responseOffersSummary.response.total_sent_waiting));
                        mCountReceived.setText(Integer.toString(responseOffersSummary.response.total_received_new));
                    }
                }
            });
    }
    public void refreshToken(){
        Repository.refreshToken(getString(R.string.oauth_client_id), mDeviceID, mRefreshToken).observe(this, new Observer<ResponseAccessToken>() {
            @Override
            public void onChanged(@Nullable ResponseAccessToken responseAccessToken) {
                if (responseAccessToken != null && responseAccessToken.error == null) {
                    RetrofitService.setToken(responseAccessToken.access_token);
                    loadUser(false);
                }
                else if(responseAccessToken != null) {
                    Log.d("error", responseAccessToken.error);
                    Log.d("error_description", responseAccessToken.error_description);

                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                            .remove(ARG_AUTH_TOKEN).remove(ARG_AUTH_REFRESH)
                            .apply();
                    RetrofitService.setToken(null);

                    NavHostFragment.findNavController(MainFragment.this).navigate(R.id.apiKeyFragment);
                }
                else {
                    Snackbar.make(mLayout, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    refreshToken();
                                }
                            }).show();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                preferences.edit()
                        .remove(ARG_API_KEY)
                        .remove(ARG_AUTH_REFRESH)
                        .remove(ARG_AUTH_TOKEN)
                        .apply();

                RetrofitService.setApiKey(null);
                NavHostFragment.findNavController(MainFragment.this).navigate(
                        R.id.apiKeyFragment, null,
                        new NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build()
                );
                return true;
            case R.id.action_tradelink:
                if(mTradeUrl != null)
                {
                    final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    final ClipData clip = ClipData.newPlainText("url", mTradeUrl);
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(mLayout, R.string.your_trade_url_copied, Snackbar.LENGTH_SHORT).show();
                }
                else if(mUser != null)
                {
                    Snackbar.make(mLayout, R.string.your_trade_url_is_loading, Snackbar.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
