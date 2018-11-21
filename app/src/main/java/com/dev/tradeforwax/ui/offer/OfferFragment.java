package com.dev.tradeforwax.ui.offer;


import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dev.tradeforwax.models.Item;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dev.tradeforwax.R;
import com.dev.tradeforwax.models.Trade;
import com.dev.tradeforwax.models.User;
import com.dev.tradeforwax.network.Repository;
import com.dev.tradeforwax.network.StatusCode;
import com.dev.tradeforwax.network.responses.ResponseOffer;
import com.dev.tradeforwax.network.responses.ResponseReport;
import com.dev.tradeforwax.ui.SteamOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;


public class OfferFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, Item.Callback {

    public static final String ARG_OFFER_ID = "offer_id";

    private int mOfferId;

    private View mLayout;

    private TextView mNicknameView;
    private View mVertifiedView;
    private ImageView mAvatarView;
    private TextView mDescriptionView;
    private TextView mStatusView;
    private TextView mCreatedView;
    private View mGiftView;
    private View mSteamView;
    private View mMessageView;


    private TextView mExpiresView;
    private View mExpiresStatic;

    private View mAcceptButton;
    private Button mDeclineButton;

    private TextView mYourCountView;
    private TextView mPartnerCountView;
    private TextView mYourPriceView;
    private TextView mPartnerPriceView;
    private RecyclerView mYourRecyclerView;
    private RecyclerView mPartnerRecyclerView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private MenuItem mReportMenuItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_offer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mOfferId = getArguments().getInt(ARG_OFFER_ID);

        mLayout = view.findViewById(R.id.layout);

        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mNicknameView = view.findViewById(R.id.nicknameView);
        mVertifiedView = view.findViewById(R.id.vertifiedView);
        mAvatarView = view.findViewById(R.id.avatarView);
        mDescriptionView = view.findViewById(R.id.descriptionView);
        mStatusView = view.findViewById(R.id.statusView);
        mCreatedView = view.findViewById(R.id.createdView);
        mSteamView = view.findViewById(R.id.steamView);
        mGiftView = view.findViewById(R.id.giftView);
        mMessageView = view.findViewById(R.id.messageView);

        mExpiresView = view.findViewById(R.id.expiresView);
        mExpiresStatic = view.findViewById(R.id.expiresStatic);

        mAcceptButton = view.findViewById(R.id.acceptButton);
        mDeclineButton = view.findViewById(R.id.declineButton);

        mYourCountView = view.findViewById(R.id.yourCountView);
        mPartnerCountView = view.findViewById(R.id.partnerCountView);
        mYourPriceView = view.findViewById(R.id.yourPriceView);
        mPartnerPriceView = view.findViewById(R.id.partnerPriceView);
        mYourRecyclerView = view.findViewById(R.id.yourRecyclerView);
        mPartnerRecyclerView = view.findViewById(R.id.partnerRecyclerView);

        mSwipeRefreshLayout = view.findViewById(R.id.swipeView);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        getOffer(false);
    }

    private void getOffer(boolean isRefresh){
        mSwipeRefreshLayout.setRefreshing(true);
        ViewModelProviders.of(this).get(OfferViewModel.class)
            .getOffer(mOfferId, isRefresh).observe(this, new Observer<ResponseOffer>() {
                @Override
                public void onChanged(@Nullable ResponseOffer responseOffer) {
                    getOffer(responseOffer);
                }
            });
    }
    private void getOffer(ResponseOffer responseOffer)
    {
        mSwipeRefreshLayout.setRefreshing(false);
        if(responseOffer == null)
            Snackbar.make(mLayout, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getOffer(true);
                        }
                    }).show();
        else if (responseOffer.status == StatusCode.OK)
        {
            final Trade trade = responseOffer.response.offer;
            final User partner = trade.sent_by_you ? trade.recipient : trade.sender;
            final User you = trade.sent_by_you ? trade.sender : trade.recipient;

            if(mReportMenuItem != null && !trade.sent_by_you) mReportMenuItem.setVisible(true);

            mNicknameView.setText(partner.display_name);
            if(partner.avatar != null && partner.getAvatar() != null)
                Picasso.get().load(partner.getAvatar()).into(mAvatarView);
            else
                mAvatarView.setImageResource(R.drawable.opskins_logo);
            mDescriptionView.setText(getString(R.string.send_0_and_recive_0, partner.items.size(), you.items.size()));

            mStatusView.setText(trade.state_name);
            mStatusView.setTextColor(trade.getStateColor());

            if(!partner.steam_id.isEmpty()) {
                mSteamView.setVisibility(View.VISIBLE);
                mSteamView.setTag(partner.steam_id);
                mSteamView.setOnClickListener(new SteamOnClickListener());
            }
            mGiftView.setVisibility(trade.is_gift ? View.VISIBLE : View.GONE);
            if(partner.verified || trade.is_case_opening)
                mVertifiedView.setVisibility(View.VISIBLE);

            final DateFormat timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            final Date timeCreated = (new Date(trade.time_created * 1000L));
            mCreatedView.setText(timeFormat.format(timeCreated));

            if (!trade.message.isEmpty())
            {
                mMessageView.setVisibility(View.VISIBLE);
                final AlertDialog messageDialog = new AlertDialog.Builder(getContext())
                        .setMessage(trade.message).setTitle(R.string.message).create();
                mMessageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        messageDialog.show();
                    }
                });
            }

            if(trade.state == Trade.STATE_ACTIVE)
            {
                final Date timeExpire = (new Date(trade.time_expires * 1000L));
                mExpiresView.setText(timeFormat.format(timeExpire));

                mDeclineButton.setVisibility(View.VISIBLE);
                mDeclineButton.setText(trade.sent_by_you ? R.string.cancel : R.string.decline);
                mDeclineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        declineOffer();
                    }
                });
                if(!trade.sent_by_you)
                {
                    mAcceptButton.setVisibility(View.VISIBLE);

                    final View dialogConfirmView = View.inflate(getContext(), R.layout.dialog_confirm_trade_simple, null);
                    final TextView twoFAcode = dialogConfirmView.findViewById(R.id.number);
                    final View confirmTrade = dialogConfirmView.findViewById(R.id.confirmTrade);
                    final TextInputLayout textInputLayout = dialogConfirmView.findViewById(R.id.textInputLayout);

                    final AlertDialog dialogConfirm = new AlertDialog.Builder(getContext())
                            .setView(dialogConfirmView).setTitle(R.string.confirm_trade).create();

                    mAcceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            textInputLayout.setErrorEnabled(false);
                            twoFAcode.setText("");

                            dialogConfirm.show();
                        }
                    });


                    confirmTrade.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String twoFA = twoFAcode.getText().toString();
                            if(twoFA.length() == 6)
                            {
                                textInputLayout.setErrorEnabled(false);
                                Repository.acceptOffer(mOfferId, twoFA).observe(OfferFragment.this, new Observer<ResponseOffer>() {
                                    @Override
                                    public void onChanged(@Nullable ResponseOffer responseOffer) {
                                        if(responseOffer == null){
                                            textInputLayout.setError(getString(R.string.opskins_not_responding));
                                            textInputLayout.setErrorEnabled(true);
                                        }
                                        else {
                                            if (responseOffer.status == StatusCode.TWOFACTOR_INCORRECT) {
                                                textInputLayout.setError("Two Factor Code is incorrect");
                                                textInputLayout.setErrorEnabled(true);
                                            } else if (responseOffer.status == StatusCode.OK) {
                                                dialogConfirm.dismiss();
                                                ViewModelProviders.of(OfferFragment.this).get(OfferViewModel.class).updateData(responseOffer);
                                            }
                                            else if(!responseOffer.message.isEmpty()){
                                                Snackbar.make(mLayout, responseOffer.message, Snackbar.LENGTH_LONG).show();
                                            }

                                            Log.d("resp", String.valueOf(responseOffer.status));
                                        }
                                    }
                                });
                            }
                            else
                            {
                                textInputLayout.setError("2FA Code should be valid");
                                textInputLayout.setErrorEnabled(true);
                            }
                        }
                    });

                }
                else mAcceptButton.setVisibility(View.GONE);
            }
            else{
                mAcceptButton.setVisibility(View.GONE);
                mDeclineButton.setVisibility(View.GONE);
                mExpiresView.setVisibility(View.GONE);
                mExpiresStatic.setVisibility(View.GONE);
            }

            mYourCountView.setText(String.valueOf(you.items.size()));
            int yourPrice = 0;
            for (int i = 0; i < you.items.size(); i++)
                yourPrice += you.items.get(i).suggested_price;
            mYourPriceView.setText(String.format("%s$", yourPrice/100f));
            mYourRecyclerView.swapAdapter(new OfferItemsAdapter(you.items, OfferFragment.this), false);

            mPartnerCountView.setText(String.valueOf(partner.items.size()));
            int partnerPrice = 0;
            for (int i = 0; i < partner.items.size(); i++)
                partnerPrice += partner.items.get(i).suggested_price;
            mPartnerPriceView.setText(String.format("%s$", partnerPrice/100f));
            mPartnerRecyclerView.swapAdapter(new OfferItemsAdapter(partner.items, OfferFragment.this), false);
        }
    }
    private void declineOffer(){
        Repository.cancelOffer(mOfferId).observe(OfferFragment.this, new Observer<ResponseOffer>() {
            @Override
            public void onChanged(@Nullable ResponseOffer responseOffer) {
                if(responseOffer == null)
                    Snackbar.make(mLayout, R.string.opskins_not_responding, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    declineOffer();
                                }
                            }).show();
                else if (responseOffer.status == StatusCode.OK)
                {
                    ViewModelProviders.of(OfferFragment.this).get(OfferViewModel.class).updateData(responseOffer);
                }
                else if(!responseOffer.message.isEmpty()){
                    Snackbar.make(mLayout, responseOffer.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void displayItem(Item item){
        item.displayItem(getContext(), null);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_offer, menu);

        mReportMenuItem = menu.findItem(R.id.action_report);

        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_report:
                final View dialogView = View.inflate(getContext(), R.layout.dialog_report, null);
                final RadioGroup group = dialogView.findViewById(R.id.radioGroup);
                final EditText messageView = dialogView.findViewById(R.id.message);

                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final AlertDialog reportDialog = builder.setView(dialogView).setTitle(R.string.report).create();

                dialogView.findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String message = messageView.getText().toString();
                        if (message.length() <= 255) {
                            reportDialog.dismiss();
                            int type = 0;
                            switch (group.getCheckedRadioButtonId()) {
                                case R.id.spam:
                                    type = 1;
                                    break;
                                case R.id.phishing:
                                    type = 2;
                                    break;
                                case R.id.error:
                                    type = 3;
                            }
                            Repository.sendReport(message, type, mOfferId).observe(OfferFragment.this, new Observer<ResponseReport>() {
                                @Override
                                public void onChanged(@Nullable ResponseReport responseReport) {
                                    if (responseReport != null && responseReport.status == StatusCode.OK && responseReport.response.success) {
                                        Snackbar.make(mLayout, "Reported", Snackbar.LENGTH_LONG).show();
                                        mReportMenuItem.setVisible(false);
                                    }else
                                        Snackbar.make(mLayout, R.string.error, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

                reportDialog.show();


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        getOffer(true);
    }
}