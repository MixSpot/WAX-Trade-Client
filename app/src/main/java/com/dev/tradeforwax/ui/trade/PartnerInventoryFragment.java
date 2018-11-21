package com.dev.tradeforwax.ui.trade;


import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.Item;
import com.dev.tradeforwax.network.responses.ResponsePartnerInventory;


public class PartnerInventoryFragment extends Fragment {

    public InventoryTradeAdapter mAdapter;
    private View mProgressBar;
    private View mEmptyState;

    private ResponsePartnerInventory mResponse = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pager_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView);
        final int columns = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL));

        mAdapter = new InventoryTradeAdapter((InventoryTradeAdapter.Callback) getParentFragment(), (Item.Callback) getParentFragment(), 1);
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar = view.findViewById(R.id.progressBar);
        mEmptyState = view.findViewById(R.id.emptyState);

        if(mResponse != null)
            refreshInventory(mResponse);
    }

    public void startRefreshInventory() {
        if(mEmptyState != null) {
            mEmptyState.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mAdapter.clearData();
        }
    }

    public void refreshInventory(ResponsePartnerInventory responseInventory)
    {
        if(mEmptyState != null) {
            mResponse = null;
            mProgressBar.setVisibility(View.GONE);
            if (responseInventory != null && responseInventory.status == 1) {
                mAdapter.updateData(responseInventory.response.items);
                if (responseInventory.response.total == 0)
                    mEmptyState.setVisibility(View.VISIBLE);
            }
        }
        else mResponse = responseInventory;
    }
}
