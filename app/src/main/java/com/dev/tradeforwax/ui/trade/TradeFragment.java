package com.dev.tradeforwax.ui.trade;


import android.annotation.SuppressLint;
import android.app.SearchManager;

import androidx.appcompat.app.AppCompatDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dev.tradeforwax.ui.offer.OfferItemsAdapter;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavOptions;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.MainActivityViewModel;
import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.App;
import com.dev.tradeforwax.models.Item;
import com.dev.tradeforwax.models.RecentPartner;
import com.dev.tradeforwax.models.User;
import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.StatusCode;
import com.dev.tradeforwax.network.responses.ResponseApps;
import com.dev.tradeforwax.network.responses.ResponseOffer;
import com.dev.tradeforwax.network.responses.ResponsePartnerInventory;
import com.dev.tradeforwax.network.responses.ResponseYourInventory;
import com.dev.tradeforwax.ui.SteamOnClickListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.fragment.NavHostFragment;

import static com.dev.tradeforwax.MainActivity.dpToPx;
import static com.dev.tradeforwax.ui.offer.OfferFragment.ARG_OFFER_ID;


@SuppressLint("RestrictedApi")
public class TradeFragment extends Fragment implements InventoryTradeAdapter.Callback, Item.Callback,OfferItemsAdapter.RemoveItemCallback {
    public static final String ARG_UID = "uid";
    public static final String ARG_TOKEN = "token";
    public static final String ARG_STEAM_ID = "steam_id";

    private static final String ARG_APP = "app";
    private static final String ARG_SORT = "sort";
    private static final String ARG_SEARCH = "search";

    private View mLayout;
    private TabLayout mTabsLayout;
    private InventoryPagerAdapter mPagerAdapter;
    private YourInventoryFragment yourInventory;
    private PartnerInventoryFragment partnerInventory;

    private TextView mYourCount;
    private TextView mYourPrice;
    private TextView mPartnerCount;
    private TextView mPartnerPrice;

    private int mCurrentApp = 0;
    private int mCurrentSort = 4;
    private String mCurrentSearch = "";
    private String mTradeMessage = "";
    private final SparseArray<App> mApps = new SparseArray<>();
    private User mUser = null;

    private int mUid;
    private String mToken;
    private String mSteamID;
    private boolean mIsSteam = false;

    private Target mTarget;
    private MenuBuilder mAppsMenu;

    private Drawable defaultApp;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentApp = savedInstanceState.getInt(ARG_APP);
            mCurrentSort = savedInstanceState.getInt(ARG_SORT);
            mCurrentSearch = savedInstanceState.getString(ARG_SEARCH);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_APP, mCurrentApp);
        outState.putInt(ARG_SORT, mCurrentSort);
        outState.putString(ARG_SEARCH, mCurrentSearch);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_trade, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        defaultApp = AppCompatResources.getDrawable(getContext(), R.drawable.ic_videogame_asset_black_24dp);
        defaultApp.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.SRC_ATOP);

        mLayout = view.findViewById(R.id.layout);

        mUid = getArguments().getInt(ARG_UID);
        mToken = getArguments().getString(ARG_TOKEN);
        mSteamID = getArguments().getString(ARG_STEAM_ID);
        if(mSteamID != null) mIsSteam = true;

        final ViewPager mPager = view.findViewById(R.id.viewPager);
        mPagerAdapter = new InventoryPagerAdapter(getChildFragmentManager(), ViewModelProviders.of(TradeFragment.this).get(TradeViewModel.class));
        mPager.setAdapter(mPagerAdapter);
        mTabsLayout = view.findViewById(R.id.tabLayout);
        mTabsLayout.setupWithViewPager(mPager);
        mTabsLayout.getTabAt(0).setText(getResources().getString(R.string.your) + " (0)");
        mTabsLayout.getTabAt(1).setText(getResources().getString(R.string.their) + " (0)");


        final BottomAppBar bar = view.findViewById(R.id.bar);
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    if(bar != null) {
                        final int dp = dpToPx(24);
                        bitmap = Bitmap.createScaledBitmap(bitmap, dp, dp, false);
                        bar.setNavigationIcon(new BitmapDrawable(getResources(), bitmap));
                    }
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    if(bar != null)
                        bar.setNavigationIcon(defaultApp);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        mAppsMenu = new MenuBuilder(getContext());
        mAppsMenu.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                final App app = mApps.get(menuItem.getItemId());

                if (!app.img.isEmpty())
                    Picasso.get().load(app.img).into(mTarget);
                else
                    bar.setNavigationIcon(defaultApp);

                mCurrentApp = app.internal_app_id;
                refreshInventories(true);

                return true;
            }
            @Override
            public void onMenuModeChange(MenuBuilder menuBuilder) {}
        });
        final MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), mAppsMenu, bar);
        menuHelper.setForceShowIcon(true);
        getApps(false);


        final View dialogView = View.inflate(getContext(), R.layout.dialog_message, null);
        final TextView messageView = dialogView.findViewById(R.id.message);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView).setTitle(R.string.offer_message)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mTradeMessage = messageView.getText().toString();
                    }
                }).create();

        bar.replaceMenu(R.menu.menu_trade_bottom);

        final PopupMenu sortMenu = new PopupMenu(getContext(), bar.findViewById(R.id.action_sort));
        sortMenu.getMenuInflater().inflate(R.menu.menu_trade_sort, sortMenu.getMenu());
        sortMenu.getMenu().getItem(mCurrentSort-1).setChecked(true);
        sortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.sort_name_asc:
                        mCurrentSort = 1;
                        break;
                    case R.id.sort_name_desc:
                        mCurrentSort = 2;
                        break;
                    case R.id.sort_last_update_asc:
                        mCurrentSort = 3;
                        break;
                    case R.id.sort_last_update_desc:
                        mCurrentSort = 4;
                        break;
                    case R.id.sort_suggested_price_asc:
                        mCurrentSort = 5;
                        break;
                    case R.id.sort_suggested_price_desc:
                        mCurrentSort = 6;
                        break;
                }
                menuItem.setChecked(true);
                refreshInventories(true);

                return false;
            }
        });

        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_message:
                        dialog.show();
                        break;
                    case R.id.action_sort:
                        sortMenu.show();
                        break;
                }
                return true;
            }
        });
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuHelper.show();
            }
        });

        final View dialogConfirmView = View.inflate(getContext(), R.layout.dialog_confirm_trade, null);
        final Toolbar toolbarDialog = dialogConfirmView.findViewById(R.id.toolbarDialog);

        final Drawable closeIcon = AppCompatResources.getDrawable(getContext(), R.drawable.ic_close_black_24dp);
        closeIcon.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
        toolbarDialog.setNavigationIcon(closeIcon);

        final TextView twoFAcode = dialogConfirmView.findViewById(R.id.number);
        final View confirmTrade = dialogConfirmView.findViewById(R.id.confirmTrade);
        final TextInputLayout textInputLayout = dialogConfirmView.findViewById(R.id.textInputLayout);
        mYourCount = dialogConfirmView.findViewById(R.id.yourCount);
        mYourPrice = dialogConfirmView.findViewById(R.id.yourPrice);
        mPartnerCount = dialogConfirmView.findViewById(R.id.partnerCount);
        mPartnerPrice = dialogConfirmView.findViewById(R.id.partnerPrice);

        final RecyclerView yourRecyclerView = dialogConfirmView.findViewById(R.id.yourRecyclerView);
        final RecyclerView partnerRecyclerView = dialogConfirmView.findViewById(R.id.partnerRecyclerView);

        final AppCompatDialog dialogConfirm = new AppCompatDialog(getContext(), R.style.AppTheme_Dialog_Fullscreen);
        final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialogConfirm.addContentView(dialogConfirmView, layoutParams);

        toolbarDialog.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogConfirm.hide();
            }
        });

        view.findViewById(R.id.trade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textInputLayout.setErrorEnabled(false);
                twoFAcode.setText("");
                final SparseArray<Item> yourSelected = yourInventory.mAdapter.mSelected;
                List<Item> yourSelectedList = new ArrayList<>(yourSelected.size());
                for (int i = 0; i < yourSelected.size(); i++)
                    yourSelectedList.add(yourSelected.valueAt(i));
                final SparseArray<Item> partnerSelected = partnerInventory.mAdapter.mSelected;
                List<Item> partnerSelectedList = new ArrayList<>(partnerSelected.size());
                for (int i = 0; i < partnerSelected.size(); i++)
                    partnerSelectedList.add(partnerSelected.valueAt(i));

                if (yourSelected.size() == 0 && partnerSelected.size() == 0)
                    Snackbar.make(view, R.string.empty_trade, Snackbar.LENGTH_SHORT).show();
                else {
                    updateCounters();

                    OfferItemsAdapter yourAdapter = new OfferItemsAdapter(yourSelectedList, mApps, TradeFragment.this);
                    yourRecyclerView.swapAdapter(yourAdapter, false);
                    OfferItemsAdapter partnerAdapter = new OfferItemsAdapter(partnerSelectedList, mApps, TradeFragment.this);
                    partnerRecyclerView.swapAdapter(partnerAdapter, false);

                    dialogConfirm.show();
                }
            }
        });



        final Observer<ResponseOffer> sendOfferObserver = new Observer<ResponseOffer>() {
            @Override
            public void onChanged(@Nullable ResponseOffer responseOffer) {
                if(responseOffer == null){
                    textInputLayout.setError(getString(R.string.opskins_not_responding));
                    textInputLayout.setErrorEnabled(true);
                }
                else {
                    if (responseOffer.status == StatusCode.TWOFACTOR_INCORRECT) {
                        textInputLayout.setError("Two Factor Code is incorrect");
                        textInputLayout.setErrorEnabled(true);
                    } else if (responseOffer.status == StatusCode.OK) {
                        dialogConfirm.dismiss();
                        final Bundle bundle = new Bundle();
                        bundle.putInt(ARG_OFFER_ID, responseOffer.response.offer.id);
                        NavHostFragment.findNavController(TradeFragment.this).navigate(
                                R.id.offerFragment,
                                bundle,
                                new NavOptions.Builder().setPopUpTo(R.id.tradeFragment, true).build()
                        );
                    } else if (responseOffer.status == StatusCode.BAD_INPUT) {
                        dialogConfirm.dismiss();
                        Snackbar.make(mLayout, R.string.trade_url_is_incorrect, Snackbar.LENGTH_SHORT).show();
                    } else
                        Snackbar.make(dialogConfirmView, responseOffer.message, Snackbar.LENGTH_LONG).show();

                    Log.d("resp", String.valueOf(responseOffer.status));
                }
            }
        };


        twoFAcode.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            confirmTrade.performClick();
                            return true;
                    }
                return false;
            }
        });
        confirmTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String twoFA = twoFAcode.getText().toString();
                if(twoFA.length() == 6)
                {
                    textInputLayout.setErrorEnabled(false);
                    if(mIsSteam)
                        Repository.sendOffer(0, mSteamID, mTradeMessage,
                                yourInventory.mAdapter.mSelected,
                                partnerInventory.mAdapter.mSelected,
                                twoFA).observe(TradeFragment.this, sendOfferObserver);
                    else
                        Repository.sendOffer(mUid, mToken, mTradeMessage,
                                yourInventory.mAdapter.mSelected,
                                partnerInventory.mAdapter.mSelected,
                                twoFA).observe(TradeFragment.this, sendOfferObserver);
                }
                else
                {
                    textInputLayout.setError("2FA Code should be valid");
                    textInputLayout.setErrorEnabled(true);
                }
            }
        });
    }

    private void updateCounters(){
        mYourCount.setText(String.valueOf(yourInventory.mAdapter.mSelected.size()));

        int yourPriceF = 0;
        for(int i = 0; i < yourInventory.mAdapter.mSelected.size(); i++)
            yourPriceF += yourInventory.mAdapter.mSelected.valueAt(i).suggested_price;
        mYourPrice.setText(String.format("%s$", yourPriceF/100f));

        mPartnerCount.setText(String.valueOf(partnerInventory.mAdapter.mSelected.size()));
        int partnerPriceF = 0;
        for(int i = 0; i < partnerInventory.mAdapter.mSelected.size(); i++)
            partnerPriceF += partnerInventory.mAdapter.mSelected.valueAt(i).suggested_price;
        mPartnerPrice.setText(String.format("%s$", partnerPriceF/100f));
    }


    private void getApps(boolean isRefresh){
        ViewModelProviders.of(getActivity()).get(TradeViewModel.class).getApps(isRefresh)
            .observe(this, new Observer<ResponseApps>() {
            @Override
            public void onChanged(@Nullable ResponseApps responseApps) {
                if(responseApps == null)
                    Snackbar.make(mLayout, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getApps(true);
                                }
                            }).show();
                else if(responseApps.status == StatusCode.OK && responseApps.response.apps != null)
                {
                    for (int i = 0; i < responseApps.response.apps.size(); i++) {
                        final App app = responseApps.response.apps.get(i);
                        mApps.put(app.internal_app_id, app);

                        if(mCurrentApp == app.internal_app_id && !app.img.isEmpty())
                            Picasso.get().load(app.img).into(mTarget);

                        if (app.isDefault == 1)
                        {
                            if(mCurrentApp == 0) {
                                mCurrentApp = app.internal_app_id;
                                if (!app.img.isEmpty())
                                    Picasso.get().load(app.img).into(mTarget);
                            }
                            refreshInventories(false);
                        }
                        final MenuItem menuItem = mAppsMenu.add(0, app.internal_app_id, 0, app.name);
                        if (!app.img.isEmpty())
                            Picasso.get().load(app.img).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    final int dp = dpToPx(24);
                                    bitmap = Bitmap.createScaledBitmap(bitmap, dp, dp, false);
                                    menuItem.setIcon(new BitmapDrawable(getResources(), bitmap));
                                }
                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    menuItem.setIcon(defaultApp);
                                }
                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {}
                            });
                        else
                            menuItem.setIcon(defaultApp);
                    }
                }
                else {
                    NavHostFragment.findNavController(TradeFragment.this).navigateUp();
                }
            }
        });
    }


    public void displayItem(Item item) {
        item.displayItem(getContext(), mApps.get(item.internal_app_id));
    }
    public void maxItemsSelected(){
        Snackbar.make(mLayout, R.string.maximum_is_100_items, Snackbar.LENGTH_SHORT).show();
    }

    public void removeItem(Item item){
        int index = yourInventory.mAdapter.mSelected.indexOfValue(item);
        if(index != -1) {
            yourInventory.mAdapter.mSelected.removeAt(index);
            yourInventory.mAdapter.notifyDataSetChanged();
            updateSelectedCount(0, yourInventory.mAdapter.mSelected.size());
            updateCounters();
        }
        else{
            index = partnerInventory.mAdapter.mSelected.indexOfValue(item);
            if(index != -1) {
                partnerInventory.mAdapter.mSelected.removeAt(index);
                partnerInventory.mAdapter.notifyDataSetChanged();
                updateSelectedCount(1, partnerInventory.mAdapter.mSelected.size());
                updateCounters();
            }
        }
    }

    private void refreshInventories(boolean isRefresh){
        mPagerAdapter.refreshYourInventory(isRefresh);
        mPagerAdapter.refreshPartnerInventory(isRefresh);
    }


    @Override
    public void updateSelectedCount(int index, int count){
        final int id = index == 0 ? R.string.your : R.string.their;
        final String text = getResources().getString(id) + " (" + count + ")";
        mTabsLayout.getTabAt(index).setText(text);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_trade, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null)
            searchView = (SearchView) searchItem.getActionView();
        if (searchView != null)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        if(!mCurrentSearch.isEmpty()){
            searchItem.expandActionView();
            searchView.setQuery(mCurrentSearch, true);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mCurrentSearch = s;
                refreshInventories(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty() && !mCurrentSearch.isEmpty())
                {
                    mCurrentSearch = "";
                    refreshInventories(true);
                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(!mCurrentSearch.isEmpty()) {
                    mCurrentSearch = "";
                    refreshInventories(true);
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private AlertDialog mUserDialog = null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_partner:
                if(mUser != null)
                {
                    if(mUserDialog == null)
                    {
                        final View dialogView = View.inflate(getContext(), R.layout.dialog_partner, null);
                        final TextView name = dialogView.findViewById(R.id.name);
                        final ImageView avatar = dialogView.findViewById(R.id.avatar);
                        final View steam = dialogView.findViewById(R.id.steamView);

                        name.setText(mUser.display_name);

                        if(mUser.avatar != null && mUser.getAvatar() != null)
                            Picasso.get().load(mUser.getAvatar()).into(avatar);
                        else
                            avatar.setImageResource(R.drawable.opskins_logo);

                        if(!mUser.steam_id.isEmpty()){
                            steam.setVisibility(View.VISIBLE);
                            steam.setTag(mUser.steam_id);
                            steam.setOnClickListener(new SteamOnClickListener());
                        }

                        mUserDialog = new AlertDialog.Builder(getContext())
                            .setView(dialogView).setTitle(R.string.your_partner).create();
                    }
                    mUserDialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class InventoryPagerAdapter extends FragmentPagerAdapter {

        private final TradeViewModel mViewModel;

        @Override
        public Fragment getItem(int position) {
            if(position == 0) return new YourInventoryFragment();
            return new PartnerInventoryFragment();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            if(position == 0) {
                yourInventory = (YourInventoryFragment) createdFragment;
                if(mCurrentApp != 0){
                    refreshYourInventory(false);
                }
            }
            else {
                partnerInventory = (PartnerInventoryFragment) createdFragment;
                if(mCurrentApp != 0){
                    refreshPartnerInventory(false);
                }
            }

            return createdFragment;
        }

        InventoryPagerAdapter(FragmentManager fm, TradeViewModel viewModel) {
            super(fm);
            mViewModel = viewModel;
        }

        @Override
        public int getCount() {
            return 2;
        }


        private void refreshYourInventory(boolean isRefresh)
        {
            if(yourInventory != null) {
                yourInventory.startRefreshInventory();
                mViewModel.getYourInventory(isRefresh, mCurrentApp, mCurrentSort, mCurrentSearch)
                    .observe(TradeFragment.this, new Observer<ResponseYourInventory>() {
                        @Override
                        public void onChanged(@Nullable ResponseYourInventory responseInventory) {
                            yourInventory.refreshInventory(responseInventory);
                        }
                    });
            }
        }
        private void refreshPartnerInventory(boolean isRefresh)
        {
            if(partnerInventory != null) {
                partnerInventory.startRefreshInventory();
                if(mIsSteam)
                    mViewModel.getPartnerInventory(isRefresh, mSteamID, mCurrentApp, mCurrentSort, mCurrentSearch)
                        .observe(TradeFragment.this, partnerInventoryObserver);
                else
                    mViewModel.getPartnerInventory(isRefresh, mUid, mCurrentApp, mCurrentSort, mCurrentSearch)
                        .observe(TradeFragment.this, partnerInventoryObserver);
            }
        }



        private final Observer<ResponsePartnerInventory> partnerInventoryObserver = new Observer<ResponsePartnerInventory>() {
            @Override
            public void onChanged(@Nullable ResponsePartnerInventory responseInventory) {
                if(mUser == null) {
                    if (responseInventory != null && responseInventory.status == StatusCode.OK) {
                        mUser = responseInventory.response.user_data;
                        ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class)
                                .addRecentPartner(
                                        new RecentPartner(mUid, mToken, mUser.steam_id,
                                                mUser.display_name, mUser.avatar, mUser.verified)
                                );
                        partnerInventory.refreshInventory(responseInventory);
                    } else if (getActivity() != null) {
//                        final Bundle bundle = new Bundle();
//                        bundle.putString("error", responseInventory != null ? responseInventory.message : getString(R.string.error));
                        NavHostFragment.findNavController(TradeFragment.this).navigateUp();
                    }
                }
                else
                    partnerInventory.refreshInventory(responseInventory);
            }
        };
    }
}
