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
        if(!colorLocal.contains("#"))
            colorLocal = "#" + colorLocal;
        return Color.parseColor(colorLocal);
    }
}
