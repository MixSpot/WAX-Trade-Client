package com.dev.tradeforwax.ui.inventory;


import android.annotation.SuppressLint;
import android.app.SearchManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dev.tradeforwax.models.Item;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.App;
import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.StatusCode;
import com.dev.tradeforwax.network.responses.ResponseApps;
import com.dev.tradeforwax.network.responses.ResponseYourInventory;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.navigation.fragment.NavHostFragment;

import static com.dev.tradeforwax.MainActivity.dpToPx;


@SuppressLint("RestrictedApi")
public class InventoryFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, Item.Callback {

    public static final String ARG_APP = "app";
    private static final String ARG_SORT = "sort";
    private static final String ARG_SEARCH = "search";

    private int mCurrentApp = 0;
    private int mCurrentSort = 4;
    private String mCurrentSearch = "";

    private View mLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private InventoryAdapter mAdapter;
    private View mEmptyState;

    private final SparseArray<App> mApps = new SparseArray<>();
    private Target mTarget;
    private MenuBuilder mAppsMenu;
    private MenuItem mGameMenuItem;

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
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_APP, mCurrentApp);
        outState.putInt(ARG_SORT, mCurrentSort);
        outState.putString(ARG_SEARCH, mCurrentSearch);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        defaultApp = AppCompatResources.getDrawable(getContext(), R.drawable.ic_videogame_asset_black_24dp);
        defaultApp.setColorFilter(getResources().getColor(R.color.accentColor), PorterDuff.Mode.SRC_ATOP);

        mLayout = view.findViewById(R.id.layout);
        mEmptyState = view.findViewById(R.id.emptyState);

        mSwipeRefreshLayout = view.findViewById(R.id.swipeView);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        final int columns = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new InventoryAdapter((AppCompatActivity) getActivity(), InventoryFragment.this);
        recyclerView.setAdapter(mAdapter);

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if(mGameMenuItem != null) {
                    final int dp = dpToPx(24);
                    bitmap = Bitmap.createScaledBitmap(bitmap, dp, dp, false);
                    mGameMenuItem.setIcon(new BitmapDrawable(getResources(), bitmap));
                }
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                if(mGameMenuItem != null)
                    mGameMenuItem.setIcon(defaultApp);
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
                    mGameMenuItem.setIcon(defaultApp);

                mCurrentApp = app.internal_app_id;
                refreshInventory();

                return true;
            }
            @Override
            public void onMenuModeChange(MenuBuilder menuBuilder) {}
        });
        getApps(false);

        view.findViewById(R.id.importView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.finishActionMode();

                final Bundle bundle = new Bundle();
                bundle.putInt(ARG_APP, mApps.get(mCurrentApp).steam_app_id);
                NavHostFragment.findNavController(InventoryFragment.this).navigate(R.id.improtOpskinsFragment, bundle);
            }
        });
    }

    @Override
    public void onRefresh() {
        refreshInventory();
    }

    private void refreshInventory(){
        mEmptyState.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mAdapter.clearData();
        Repository.getYourInventory(mCurrentApp, mCurrentSort, mCurrentSearch)
                .observe(this, new Observer<ResponseYourInventory>() {
            @Override
            public void onChanged(@Nullable ResponseYourInventory responseInventory) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (responseInventory == null)
                    Snackbar.make(mLayout, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    refreshInventory();
                                }
                            }).show();
                else if(responseInventory.status == StatusCode.OK) {
                    mAdapter.updateData(responseInventory.response.items);
                    if (responseInventory.response.total == 0)
                        mEmptyState.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void getApps(boolean isRefresh){
        ViewModelProviders.of(InventoryFragment.this).get(InventoryViewModel.class).getApps(isRefresh)
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
                        else if(responseApps.status == StatusCode.OK) {
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

                                    refreshInventory();
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
                    }
                });
    }

    public void displayItem(Item item){
        item.displayItem(getContext(), mApps.get(item.internal_app_id));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);

        mGameMenuItem = menu.findItem(R.id.action_game);
        if(mApps.size() != 0)
            Picasso.get().load(mApps.get(mCurrentApp).img).into(mTarget);
        else
            mGameMenuItem.setIcon(defaultApp);


        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }

        if(!mCurrentSearch.isEmpty()){
            searchItem.expandActionView();
            searchView.setQuery(mCurrentSearch, true);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mCurrentSearch = s;
                refreshInventory();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty() && !mCurrentSearch.isEmpty())
                {
                    mCurrentSearch = "";
                    refreshInventory();
                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(!mCurrentSearch.isEmpty()) {
                    mCurrentSearch = "";
                    refreshInventory();
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                PopupMenu sortMenu = new PopupMenu(getContext(), getActivity().findViewById(R.id.action_sort));
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

                        refreshInventory();

                        return true;
                    }
                });
                sortMenu.show();
                return true;
            case R.id.action_game:
                final MenuPopupHelper appsPopup = new MenuPopupHelper(getContext(), mAppsMenu, getActivity().findViewById(R.id.action_game));
                appsPopup.setForceShowIcon(true);
                appsPopup.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
