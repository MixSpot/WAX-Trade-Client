package com.dev.tradeforwax.ui.inventory;


import android.os.Build;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.Item;
import com.dev.tradeforwax.network.Repository;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final AppCompatActivity mContext;
    private final Item.Callback mItemCallback;

    private int mStatusbarColor;

    private List<Item> mData = new ArrayList<>();
    private final List<Integer> mSelected = new ArrayList<>();
    private ActionMode mActionMode;

    public InventoryAdapter(AppCompatActivity context, Item.Callback itemCallback){
        mContext = context;
        mItemCallback = itemCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_item, parent, false);
        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Item item = mData.get(position);

        holder.name.setText(item.name);
        holder.name.setSelected(true);
        holder.category.setText(item.category);
        if(item.color != null && !item.color.isEmpty())
            holder.category.setTextColor(item.getColor());
        else
            holder.category.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryTextColor));

        holder.price.setText(item.getPrice());

        final String url;
        if(item.preview_urls != null && item.preview_urls.thumb_image != null && !item.preview_urls.thumb_image.isEmpty())
            url = item.preview_urls.thumb_image;
        else
            url = item.image.px600;
        Picasso.get().load(url).into(holder.image);

        holder.selected.setVisibility(mSelected.contains(position) ? View.VISIBLE : View.GONE);
        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position, boolean isLong) {
                if(isLong)
                    mItemCallback.displayItem(mData.get(position));
                else
                {
                    if (mActionMode == null)
                        mActionMode = mContext.startSupportActionMode(new ActionMode.Callback() {
                            @Override
                            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                                mode.getMenuInflater().inflate(R.menu.menu_inventory_selection, menu);
                                mode.setTitle(R.string.withdraw_to_opskins);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    mStatusbarColor = mContext.getWindow().getStatusBarColor();
                                    mContext.getWindow().setStatusBarColor(0xFF111111);
                                }
                                return true;
                            }

                            @Override
                            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                                return false;
                            }

                            @Override
                            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_confirm:
                                        final StringBuilder strbul = new StringBuilder();

                                        Collections.sort(mSelected);
                                        Collections.reverse(mSelected);

                                        final Iterator<Integer> iter = mSelected.iterator();
                                        while (iter.hasNext()) {
                                            int selectedPosition = iter.next();
                                            strbul.append(mData.get(selectedPosition).id);
                                            if (iter.hasNext()) strbul.append(",");

                                            mData.remove(selectedPosition);
                                        }
                                        notifyDataSetChanged();
                                        //todo listen to response
                                        Repository.withdrawToOpskins(strbul.toString());

                                        mActionMode.finish();
                                        mActionMode = null;

                                        return true;
                                    default:
                                        return false;
                                }
                            }

                            @Override
                            public void onDestroyActionMode(ActionMode mode) {
                                mSelected.clear();
                                notifyDataSetChanged();
                                mActionMode = null;

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    mContext.getWindow().setStatusBarColor(mStatusbarColor);
                            }
                        });

                    toggleSelection(position);
                }
            }
        });


        if(item.wear != null) {
            holder.wearbar.setVisibility(View.VISIBLE);

            final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.wearpointer.getLayoutParams();
            params.guidePercent = item.wear;
            holder.wearpointer.setLayoutParams(params);
        }
        else
            holder.wearbar.setVisibility(View.GONE);
    }

    public void finishActionMode(){
        if(mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    private void toggleSelection(int pos) {
        if (mSelected.contains(pos))
        {
            mSelected.remove(Integer.valueOf(pos));
            int size = mSelected.size();
            if(size == 0)
            {
                mActionMode.finish();
                mActionMode = null;
            }
            else
                mActionMode.setSubtitle(Integer.toString(size));
        }
        else
        {
            mSelected.add(pos);
            mActionMode.setSubtitle(Integer.toString(mSelected.size()));
        }

        notifyItemChanged(pos);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void clearData()
    {
        if(mData.size() > 0) {
            mData.clear();
            notifyDataSetChanged();
        }
    }
    public void updateData(List<Item> data)
    {
        mData = data;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener
    {
        private final ImageView image;
        private final TextView name;
        private final TextView category;
        private final TextView price;

        private final View selected;

        private final View wearbar;
        private final Guideline wearpointer;

        private ItemClickListener clickListener;
        public ViewHolder(View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            category = itemView.findViewById(R.id.category);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.image);

            selected = itemView.findViewById(R.id.selected);

            wearbar = itemView.findViewById(R.id.wearBar);
            wearpointer = itemView.findViewById(R.id.wearPointer);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        public void setClickListener(ItemClickListener itemClickListener)
        {
            this.clickListener = itemClickListener;
        }
        @Override
        public void onClick(View view)
        {
            clickListener.onClick(getAdapterPosition(), false);
        }
        @Override
        public boolean onLongClick(View view)
        {
            clickListener.onClick(getAdapterPosition(), true);
            return true;
        }

    }

    public interface ItemClickListener {
        void onClick(int position, boolean isLong);
    }
}
