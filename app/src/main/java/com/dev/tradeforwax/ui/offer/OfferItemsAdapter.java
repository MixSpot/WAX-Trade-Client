package com.dev.tradeforwax.ui.offer;



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
import com.dev.tradeforwax.models.App;
import com.dev.tradeforwax.models.Item;
import com.dev.tradeforwax.ui.trade.TradeFragment;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OfferItemsAdapter extends RecyclerView.Adapter<OfferItemsAdapter.ViewHolder> {

    private final List<Item> mData;
    private SparseArray<App> mApps = null;
    private final Item.Callback mItemCallback;
    private RemoveItemCallback mRemoveItemCallback;

    public OfferItemsAdapter(List<Item> data, Item.Callback itemCallback){
        mData = data;
        mItemCallback = itemCallback;
    }
    public OfferItemsAdapter(List<Item> data, SparseArray<App> apps, TradeFragment fragment){
        mData = data;
        mApps = apps;
        mItemCallback = fragment;
        mRemoveItemCallback = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_offer_item, parent, false);
        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Item item = mData.get(position);

        holder.name.setText(item.name);
        holder.name.setSelected(true);

        final String url;
        if(item.preview_urls != null && item.preview_urls.thumb_image != null && !item.preview_urls.thumb_image.isEmpty())
            url = item.preview_urls.thumb_image;
        else
            url = item.image.px600;
        Picasso.get().load(url).into(holder.image);

        holder.price.setText(item.getPrice());

        holder.category.setText(item.category);
        if(item.color != null && !item.color.equals(""))
            holder.category.setTextColor(item.getColor());
        else
            holder.category.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryTextColor));

        if(mApps != null) {
            holder.app.setVisibility(View.VISIBLE);
            Picasso.get().load(mApps.get(item.internal_app_id).img).into(holder.app);

            holder.remove.setVisibility(View.VISIBLE);
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mData.remove(item);
                    mRemoveItemCallback.removeItem(item);
                    notifyDataSetChanged();
                }
            });
        }

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position) {
                mItemCallback.displayItem(mData.get(position));
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


    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener
    {
        private final ImageView image;
        private final TextView name;
        private final TextView category;
        private final TextView price;

        private final ImageView app;
        private final ImageView remove;

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

            app = itemView.findViewById(R.id.appImageView);
            remove = itemView.findViewById(R.id.removeView);

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
            clickListener.onClick(getAdapterPosition());
        }
        @Override
        public boolean onLongClick(View view)
        {
            clickListener.onClick(getAdapterPosition());
            return true;
        }

    }

    public interface ItemClickListener {
        void onClick(int position);
    }
    public interface RemoveItemCallback {
        void removeItem(Item item);
    }
}
