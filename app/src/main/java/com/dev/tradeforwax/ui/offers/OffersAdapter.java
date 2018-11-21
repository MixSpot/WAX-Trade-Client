package com.dev.tradeforwax.ui.offers;


import android.app.AlertDialog;
import android.content.res.Resources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.Trade;
import com.dev.tradeforwax.models.User;
import com.dev.tradeforwax.ui.SteamOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.ViewHolder> {

    private List<Trade> mData = new ArrayList<>();

    private final AlertDialog mMessageDialog;
    private final DateFormat mTimeFormat;

    private final Resources mContext;
    private final Callback mCallback;

    public interface Callback {
        void onOfferClick(int offerId);
    }
    public OffersAdapter(Fragment context){
        mContext = context.getResources();
        mCallback = (Callback) context;
        mMessageDialog = new AlertDialog.Builder(context.getContext()).setTitle(R.string.message).create();

        mTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.view_offer, parent, false);
        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Trade offer = mData.get(position);
        final User partner = offer.sent_by_you ? offer.recipient : offer.sender;
        final User you = offer.sent_by_you ? offer.sender : offer.recipient;

        holder.nickname.setText(partner.display_name);
        if(partner.avatar != null && partner.getAvatar() != null)
            Picasso.get().load(partner.getAvatar()).into(holder.avatar);
        else
            holder.avatar.setImageResource(R.drawable.opskins_logo);

        holder.status.setText(offer.state_name);
        holder.status.setTextColor(offer.getStateColor());


        final Date timeCreated = new Date(offer.time_created * 1000L);
        holder.created.setText(mTimeFormat.format(timeCreated));

        holder.description.setText(mContext.getString(R.string.send_0_and_recive_0, partner.items.size(), you.items.size()));

        if(!offer.message.isEmpty()) {
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMessageDialog.setMessage(offer.message);
                    mMessageDialog.show();
                }
            });
        }
        else
            holder.message.setVisibility(View.GONE);

        if(!partner.steam_id.isEmpty()) {
            holder.steam.setVisibility(View.VISIBLE);
            holder.steam.setTag(partner.steam_id);
            holder.steam.setOnClickListener(new SteamOnClickListener());
        }
        else
            holder.steam.setVisibility(View.GONE);

        holder.vertified.setVisibility((partner.verified || offer.is_case_opening) ? View.VISIBLE : View.GONE);
        holder.gift.setVisibility(offer.is_gift ? View.VISIBLE : View.GONE);

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position) {
                mCallback.onOfferClick(mData.get(position).id);
            }
        });
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
    public void updateData(List<Trade> data)
    {
        mData = data;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private final TextView nickname;
        private final ImageView avatar;

        private final TextView description;
        private final View message;
        private final View gift;
        private final View steam;
        private final View vertified;

        private final TextView status;
        private final TextView created;

        private ItemClickListener clickListener;
        public ViewHolder(View itemView)
        {
            super(itemView);
            nickname = itemView.findViewById(R.id.nicknameView);
            avatar = itemView.findViewById(R.id.avatarView);

            description = itemView.findViewById(R.id.descriptionView);
            message = itemView.findViewById(R.id.messageView);
            gift = itemView.findViewById(R.id.giftView);
            steam = itemView.findViewById(R.id.steamView);
            vertified = itemView.findViewById(R.id.vertifiedView);

            status = itemView.findViewById(R.id.statusView);
            created = itemView.findViewById(R.id.createdView);

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
