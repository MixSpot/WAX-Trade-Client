package com.dev.tradeforwax.ui.main;


import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.MainActivity;
import com.dev.tradeforwax.MainActivityViewModel;
import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.RecentPartner;
import com.dev.tradeforwax.ui.trade.TradeFragment;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import androidx.navigation.Navigation;

public class RecentPartnersAdapter extends RecyclerView.Adapter<RecentPartnersAdapter.ViewHolder> {

    private final List<RecentPartner> mData;
    private final Context mContext;

    private boolean isActive = false;

    public RecentPartnersAdapter(Context context, List<RecentPartner> data){
        mContext = context;
        Collections.reverse(data);
        mData = data;
    }

    public void setActive(){
        isActive = true;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_recent_partner, parent, false);
        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RecentPartner recentPartner = mData.get(position);

        holder.name.setText(recentPartner.name);
        if(recentPartner.avatar != null && recentPartner.getAvatar() != null)
            Picasso.get().load(recentPartner.getAvatar()).into(holder.avatar);
        else
            holder.avatar.setImageResource(R.drawable.opskins_logo);

        holder.vertified.setVisibility(recentPartner.verified ? View.VISIBLE : View.GONE);

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewModelProviders.of((MainActivity) mContext).get(MainActivityViewModel.class).removeRecentPartner(recentPartner);
                final int pos = holder.getAdapterPosition();
                mData.remove(pos);
                notifyItemRemoved(pos);
            }
        });

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position) {
                if(isActive) {
                    final Bundle bundle = new Bundle();

                    if (!recentPartner.steam_id.isEmpty())
                        bundle.putString(TradeFragment.ARG_STEAM_ID, recentPartner.steam_id);
                    else {
                        bundle.putInt(TradeFragment.ARG_UID, recentPartner.uid);
                        bundle.putString(TradeFragment.ARG_TOKEN, recentPartner.token);
                    }

                    Navigation.findNavController(holder.itemView).navigate(R.id.tradeFragment, bundle);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private final ImageView avatar;
        private final TextView name;
        private final View vertified;
        private final View remove;

        private ItemClickListener clickListener;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nicknameView);
            avatar = itemView.findViewById(R.id.avatarView);
            vertified = itemView.findViewById(R.id.vertifiedView);
            remove = itemView.findViewById(R.id.removeView);
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
