package com.dev.tradeforwax.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class RecentPartner implements Serializable, Comparable<RecentPartner> {

    public final int uid;
    public final String token;
    public final String steam_id;
    public final String name;
    public final String avatar;
    public final boolean verified;
    public int order;

    @Override
    public int compareTo(@NonNull RecentPartner o) {
        if (order > o.order)
            return 1;
        else if (order < o.order)
            return -1;

        return 0;
    }

//    public RecentPartner(String hash){
//        final String[] array = hash.split("\\|", 5);
//        uid = array[0];
//        token = array[1];
//        avatar = array[2];
//        verified = array[3].equals("1");
//        name = array[4];
//    }
    public RecentPartner(int uid, String token, String steam_id, String name, String avatar, boolean verified){
        this.uid = uid;
        this.token = token;
        this.steam_id = steam_id;
        this.name = name;
        this.avatar = avatar;
        this.verified = verified;
    }

    public String getAvatar(){
        if(avatar != null){
            if(avatar.equals("/images/opskins-logo-avatar.png"))
                return null;
            else if(avatar.startsWith("/"))
                return "https://opskins.com"+avatar;
        }

        return avatar;
    }

//    @Override
//    public String toString() {
//        return uid+"|"+token+"|"+avatar+"|"+(verified?"1":"0")+"|"+name;
//    }
}
