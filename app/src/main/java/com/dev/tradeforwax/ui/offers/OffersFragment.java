package com.dev.tradeforwax.ui.offers;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.Trade;
import com.dev.tradeforwax.network.responses.ResponseOffers;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.fragment.NavHostFragment;

import static com.dev.tradeforwax.ui.offer.OfferFragment.ARG_OFFER_ID;


public class OffersFragment extends Fragment implements OffersAdapter.Callback {

    private String mType = "received";
    private String mSort = "modified";
    private final List<Integer> mFilter = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private OffersAdapter mAdapter;
    private View mProgressBar;
    private View mEmptyState;
    private View mClearFilterView;

    private PopupMenu mSortMenu = null;
    private PopupMenu mFilterMenu = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_offers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mAdapter = new OffersAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar = view.findViewById(R.id.progressBar);
        mEmptyState = view.findViewById(R.id.emptyState);
        mClearFilterView = view.findViewById(R.id.clearFilterButton);
        mClearFilterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterMenu = null;
                mFilter.clear();
                refreshOffers(true);
            }
        });

        final TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        if(ViewModelProviders.of(OffersFragment.this).get(OffersViewModel.class).type == 1)
            tabLayout.getTabAt(1).select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) mType = "received";
                else mType = "sent";

                mFilterMenu = null;
                mFilter.clear();
                refreshOffers(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                refreshOffers(true);
            }
        });

        refreshOffers(false);
    }


    private void refreshOffers(boolean isRefresh)
    {
        final OffersViewModel viewModel = ViewModelProviders.of(OffersFragment.this).get(OffersViewModel.class);

        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyState.setVisibility(View.GONE);
        mClearFilterView.setVisibility(View.GONE);
        mAdapter.clearData();
        viewModel.getOffers(mSort, mType, mFilter, isRefresh).observe(this, new Observer<ResponseOffers>() {
            @Override
            public void onChanged(@Nullable ResponseOffers responseOffers) {
                mProgressBar.setVisibility(View.GONE);
                if(responseOffers == null){
                    Snackbar.make(mRecyclerView, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    refreshOffers(true);
                                }
                            }).show();
                }
                else if ( responseOffers.status == 1) {
                    mAdapter.updateData(responseOffers.response.offers);
                    if (responseOffers.response.total == 0) {
                        mEmptyState.setVisibility(View.VISIBLE);
                        if (!mFilter.isEmpty())
                            mClearFilterView.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (getActivity() != null)
                        NavHostFragment.findNavController(OffersFragment.this).navigateUp();
                }
            }
        });
    }

    @Override
    public void onOfferClick(int offerId){
        final Bundle bundle = new Bundle();
        bundle.putInt(ARG_OFFER_ID, offerId);
        NavHostFragment.findNavController(OffersFragment.this).navigate(R.id.offerFragment, bundle);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_offers, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                if(mSortMenu == null){
                    mSortMenu = new PopupMenu(getContext(), getActivity().findViewById(R.id.action_sort));
                    mSortMenu.getMenuInflater().inflate(R.menu.menu_offers_sort, mSortMenu.getMenu());
                    mSortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()){
                                case R.id.sort_created:
                                    mSort = "created";
                                    break;
                                case R.id.sort_expired:
                                    mSort = "expired";
                                    break;
                                case R.id.sort_modified:
                                    mSort = "modified";
                                    break;
                            }
                            menuItem.setChecked(true);
                            refreshOffers(true);

                            return false;
                        }
                    });
                }
                mSortMenu.show();

                return true;
            case R.id.action_filter:
                if(mFilterMenu == null){
                    mFilterMenu = new PopupMenu(getContext(), getActivity().findViewById(R.id.action_filter));
                    mFilterMenu.getMenuInflater().inflate(R.menu.menu_offers_filter, mFilterMenu.getMenu());
                    if (mType.equals("received"))
                        mFilterMenu.getMenu().findItem(R.id.filter_waiting).setVisible(false);
                    else{
                        mFilterMenu.getMenu().findItem(R.id.filter_new).setVisible(false);
                        mFilterMenu.getMenu().findItem(R.id.filter_pending).setVisible(false);
                    }

                    mFilterMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            final Integer id;
                            switch (menuItem.getItemId()){
                                case R.id.filter_waiting:
                                case R.id.filter_new:
                                    id = Trade.STATE_ACTIVE;
                                    break;
                                case R.id.filter_pending:
                                    id = Trade.STATE_PENDING_CASE_OPEN;
                                    break;
                                case R.id.filter_accepted:
                                    id = Trade.STATE_ACCEPTED;
                                    break;
                                case R.id.filter_expired:
                                    id = Trade.STATE_EXPIRED;
                                    break;
                                case R.id.filter_declined:
                                    id = Trade.STATE_DECLINED;
                                    break;
                                case R.id.filter_canceled:
                                    id = Trade.STATE_CANCELED;
                                    break;
                                default:
                                    id = 0;
                            }

                            if(mFilter.contains(id)) {
                                mFilter.remove(id);
                                menuItem.setChecked(false);
                            }
                            else {
                                mFilter.add(id);
                                menuItem.setChecked(true);
                            }

                            refreshOffers(true);
                            return true;
                        }
                    });
                }
                mFilterMenu.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
