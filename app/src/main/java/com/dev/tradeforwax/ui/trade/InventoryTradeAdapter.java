package com.dev.tradeforwax.ui.trade;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.Item;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class InventoryTradeAdapter extends RecyclerView.Adapter<InventoryTradeAdapter.ViewHolder> {

    private List<Item> mData = new ArrayList<>();
    public final SparseArray<Item> mSelected = new SparseArray<>();
    private final int mIndex;
    private final Callback mCallback;
    private final Item.Callback mItemCallback;

    public interface Callback {
        void updateSelectedCount(int index, int count);
        void maxItemsSelected();
    }
    public InventoryTradeAdapter(Callback callback, Item.Callback itemCallback, int index){
        mIndex = index;
        mCallback = callback;
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
        if(item.color != null && !item.color.equals(""))
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

        holder.selected.setVisibility(mSelected.indexOfKey(item.id) >= 0 ? View.VISIBLE : View.GONE);

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position, boolean isLong) {
                final Item item = mData.get(position);
                if (isLong)
                    mItemCallback.displayItem(item);
                else{
                    if (mSelected.indexOfKey(item.id) >= 0)
                        mSelected.remove(item.id);
                    else {
                        if (mSelected.size() == 100) {
                            mCallback.maxItemsSelected();
                            return;
                        } else
                            mSelected.put(item.id, item);
                    }

                    mCallback.updateSelectedCount(mIndex, mSelected.size());

                    notifyItemChanged(position);
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

        private final View wearbar;
        private final Guideline wearpointer;

        private final View selected;

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
        public boolean onLongClick(View view) {
            clickListener.onClick(getAdapterPosition(), true);
            return true;
        }

    }

    public interface ItemClickListener {
        void onClick(int position, boolean isLong);
    }
}
