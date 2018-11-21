package com.dev.tradeforwax.ui.import_opskins;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.OpskinsInventoryItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImportOpskinsAdapter extends RecyclerView.Adapter<ImportOpskinsAdapter.ViewHolder> {

    public List<OpskinsInventoryItem> mData = new ArrayList<>();
    public final List<Integer> mSelected = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_item, parent, false);
        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final OpskinsInventoryItem item = mData.get(position);

        holder.name.setText(item.market_name);
        holder.name.setSelected(true);
        holder.category.setText(item.type);
        if(item.color != null && !item.color.equals(""))
            holder.category.setTextColor(item.getColor());
        else
            holder.category.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryTextColor));

        Picasso.get().load(item.img).into(holder.image);

        holder.selected.setVisibility(mSelected.contains(position) ? View.VISIBLE : View.GONE);
        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position) {
                toggleSelection(position);
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

    private void toggleSelection(int pos) {
        if (mSelected.contains(pos))
        {
            mSelected.remove(Integer.valueOf(pos));
        }
        else
        {
            mSelected.add(pos);
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
    public void updateData(List<OpskinsInventoryItem> data)
    {
        mData = data;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private final ImageView image;
        private final TextView name;
        private final TextView category;

        private final View selected;

        private final View wearbar;
        private final Guideline wearpointer;

        private ItemClickListener clickListener;
        public ViewHolder(View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            category = itemView.findViewById(R.id.category);
            image = itemView.findViewById(R.id.image);

            selected = itemView.findViewById(R.id.selected);

            wearbar = itemView.findViewById(R.id.wearBar);
            wearpointer = itemView.findViewById(R.id.wearPointer);
            itemView.setOnClickListener(this);
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

    }

    public interface ItemClickListener {
        void onClick(int position);
    }
}
