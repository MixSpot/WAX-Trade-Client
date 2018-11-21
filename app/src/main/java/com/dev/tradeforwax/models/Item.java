package com.dev.tradeforwax.models;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tradeforwax.MainActivity;
import com.dev.tradeforwax.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;

public class Item {

    public int id;
    public int internal_app_id;
    public int sku;
    public Float wear;
    public int trade_hold_expires;
    public String name;
    public String category;
    public String rarity;
    public String type;
    public String color;

    public Images image;

    public int suggested_price;
    public int suggested_price_floor;
    public PreviewUrls preview_urls;
    @Nullable
    public String inspect;
    @Nullable
    public String eth_inspect;
    public int pattern_index;
    public int paint_index;
    public int wear_tier_index;

    public Float getPriceFloat(){
        return suggested_price/100f;
    }
    public String getPrice(){
        return getPriceFloat() + "$";
    }
    public int getColor(){
        String colorLocal = color;
        if(!colorLocal.contains("#"))
            colorLocal = "#" + colorLocal;
        return Color.parseColor(colorLocal);
    }

    public interface Callback{
        void displayItem(Item item);
    }

    public void displayItem(Context context, App app)
    {
        final View dialogView = View.inflate(context, R.layout.dialog_item, null);
        ((TextView)dialogView.findViewById(R.id.titleView)).setText(name);
        ((TextView)dialogView.findViewById(R.id.priceView)).setText(getPriceFloat().toString());

        if(app != null) {
            ((TextView) dialogView.findViewById(R.id.gameTextView)).setText(app.name);
            Picasso.get().load(app.img).into((ImageView) dialogView.findViewById(R.id.gameImageView));
        }
        else {
            dialogView.findViewById(R.id.gameGroup).setVisibility(View.GONE);
            Guideline guideline = dialogView.findViewById(R.id.guideline);
            guideline.setGuidelineBegin(MainActivity.dpToPx(8));
            guideline.setGuidelinePercent(-1.0F);
        }

        final String url;
        if(preview_urls != null && preview_urls.front_image != null)
            url = preview_urls.front_image;
        else if(preview_urls != null && preview_urls.thumb_image != null)
            url = preview_urls.thumb_image;
        else
            url = image.px600;
        Picasso.get().load(url).into((ImageView) dialogView.findViewById(R.id.itemImageView));

        final TextView categoryView = dialogView.findViewById(R.id.categoryTextView);
        categoryView.setText(category);
        if(color != null && !color.equals(""))
            categoryView.setTextColor(getColor());
        else
            categoryView.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));

        if(wear != null) {
            dialogView.findViewById(R.id.wearBar).setVisibility(View.VISIBLE);
            final Guideline wearPonter = dialogView.findViewById(R.id.wearPointer);
            final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) wearPonter.getLayoutParams();
            params.guidePercent = wear;
            wearPonter.setLayoutParams(params);

            final TextView wearText = dialogView.findViewById(R.id.wearTextView);
            wearText.setVisibility(View.VISIBLE);
            wearText.setText(context.getString(R.string.wear, ((Float)(wear*100)).toString()));
        }

        new AlertDialog.Builder(context).setView(dialogView).create().show();
    }
}
