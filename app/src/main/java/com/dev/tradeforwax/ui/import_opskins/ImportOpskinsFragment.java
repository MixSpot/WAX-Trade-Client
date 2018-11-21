package com.dev.tradeforwax.ui.import_opskins;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.responses.Response;
import com.dev.tradeforwax.network.responses.ResponseOpskinsInventory;

import java.util.Iterator;

import androidx.navigation.fragment.NavHostFragment;

import static com.dev.tradeforwax.ui.inventory.InventoryFragment.ARG_APP;


public class ImportOpskinsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private int mCurrentApp = 0;

    private View mLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImportOpskinsAdapter mAdapter;
    private View mEmptyState;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_import_opskins, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mCurrentApp = getArguments().getInt(ARG_APP);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mLayout = view.findViewById(R.id.layout);
        mEmptyState = view.findViewById(R.id.emptyState);

        mSwipeRefreshLayout = view.findViewById(R.id.swipeView);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        final RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView);
        final int columns = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new ImportOpskinsAdapter();
        mRecyclerView.setAdapter(mAdapter);


        refreshInventory(false);
    }

    @Override
    public void onRefresh() {
        refreshInventory(true);
    }

    private void refreshInventory(boolean isRefresh){
        mEmptyState.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mAdapter.clearData();
        ViewModelProviders.of(ImportOpskinsFragment.this).get(ImportOpskinsViewModel.class).getInventory(mCurrentApp, isRefresh)
                .observe(this, new Observer<ResponseOpskinsInventory>() {
                    @Override
                    public void onChanged(@Nullable ResponseOpskinsInventory responseInventory) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (responseInventory != null && responseInventory.status == 1) {
                            mAdapter.updateData(responseInventory.response.items);
                            if (responseInventory.response.items.size() == 0)
                                mEmptyState.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_import_opskins, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import:
                if(!mAdapter.mSelected.isEmpty()) {
                    mLayout.setEnabled(false);
                    mSwipeRefreshLayout.setRefreshing(true);

                    final StringBuilder strbul = new StringBuilder();
                    final Iterator<Integer> iter = mAdapter.mSelected.iterator();
                    while (iter.hasNext()) {
                        int selectedPosition = iter.next();
                        strbul.append(mAdapter.mData.get(selectedPosition).id);
                        if (iter.hasNext()) strbul.append(",");
                    }
                    importFromOpskins(strbul.toString());
                }
                else
                    Snackbar.make(mLayout, R.string.you_must_select_items, Snackbar.LENGTH_SHORT).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void importFromOpskins(final String items){
        Repository.importFromOpskins(items).observe(this, new Observer<Response>() {
            @Override
            public void onChanged(@Nullable Response response) {
                if(response == null)
                    Snackbar.make(mLayout, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    importFromOpskins(items);
                                }
                            }).show();
                else
                    NavHostFragment.findNavController(ImportOpskinsFragment.this).navigateUp();
            }
        });
    }
}
