package com.dev.tradeforwax.models;

import android.graphics.Color;

public class OpskinsInventoryItem {

    public int id;
    public int appid;
    public Float wear;
    public String market_name;
    public String type;
    public String color;
    public String img;

    public int getColor(){
        String colorLocal = color;
        if(colorLocal.length() == 3)
            colorLocal = String.valueOf(colorLocal.charAt(0)) + colorLocal.charAt(0) +
                    colorLocal.charAt(1) + colorLocal.charAt(1) +
                    colorLocal.charAt(2) + colorLocal.charAt(2);
        if(!colorLocal.contains("#"))
            colorLocal = "#" + colorLocal;
        return Color.parseColor(colorLocal);
    }
}
