package com.dev.tradeforwax.models;

import android.graphics.Color;

public class Trade {

    public final static int STATE_ACTIVE = 2;
    public final static int STATE_ACCEPTED = 3;
    public final static int STATE_EXPIRED = 5;
    public final static int STATE_CANCELED = 6;
    public final static int STATE_DECLINED = 7;
    public final static int STATE_INVALID_ITEMS = 8;
    public final static int STATE_PENDING_CASE_OPEN = 9;
    public final static int STATE_EXPIRED_CASE_OPEN = 10;
    public final static int STATE_FAILED_CASE_OPEN  = 12;

    public int id;
    public User sender;
    public User recipient;

    public int state;
    public String state_name;

    public int time_created;
    public int time_updated;
    public int time_expires;

    public String message;

    public boolean is_gift;
    public boolean is_case_opening;
    public boolean sent_by_you;

    public int getStateColor(){
        switch (this.state){
            case STATE_CANCELED:
            case STATE_DECLINED:
            case STATE_EXPIRED:
            case STATE_INVALID_ITEMS:
                return Color.parseColor("#e53935");
            case STATE_ACCEPTED:
                return Color.parseColor("#43a047");
        }
        return Color.parseColor("#00acc1");
    }
}
