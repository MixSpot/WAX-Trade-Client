package com.dev.tradeforwax.models;


import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {

    @SerializedName(value="id", alternate={"uid"})
    public int id;
    @Nullable
    public String steam_id;
    @SerializedName(value="display_name", alternate={"username"})
    public String display_name;
    public String avatar;
    public boolean verified;

    public boolean twofactor_enabled;
    public boolean inventory_is_private;
    public boolean allow_twofactor_code_reuse;

    @Nullable
    public List<Item> items;

    public String getAvatar(){
        if(avatar != null){
            if(avatar.equals("/images/opskins-logo-avatar.png"))
                return null;
            else if(avatar.startsWith("/"))
                return "https://opskins.com"+avatar;
        }

        return avatar;
    }
}
