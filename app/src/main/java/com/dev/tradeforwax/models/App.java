package com.dev.tradeforwax.models;

import com.google.gson.annotations.SerializedName;

public class App {

    public int internal_app_id;
    public int steam_app_id;
    public int steam_context_id;
    public String name;
    public String long_name;
    public String img;

    @SerializedName("default")
    public int isDefault;
}
