package com.dev.tradeforwax.network;

public final class StatusCode {

    public static final int OK = 1;

    public static int GENERIC_USER_ACCOUNT_ERROR = 102;
    public static int ACCESS_DENIED = 106;
    public static int NOT_LOGGED_IN = 108;
    public static int NEEDS_TWOFACTOR = 112;
    public static final int TWOFACTOR_INCORRECT = 122;
    public static int USERNAME_TAKEN = 124;
    public static int UNACCEPTABLE_USERNAME = 126;

    public static int GENERIC_INTERNAL_ERROR = 202;
    public static int DATABASE_ERROR = 204;
    public static int NOT_FOUND = 206;
    public static int BAD_STATE = 208;
    public static int NO_MATCHING_ITEMS_FOUND = 210;
    public static int CANNOT_CREATE_DIRECTORY = 216;
    public static int FILE_UPLOAD_ERROR = 218;
    public static int FILE_UPLOAD_ALREADY_EXISTS = 220;
    public static int CANNOT_DELETE_FILE = 222;
    public static int ALREADY_IN_THAT_STATE = 226;
    public static int LOCKED = 228;
    public static int DISABLED = 234;
    public static int MALFORMED_RESPONSE = 236;
    public static int EXPIRED = 238;
    public static int EMPTY_DATA = 240;
    public static int ITEM_NEEDS_REPAIR = 246;
    public static int ITEM_NOT_IN_INVENTORY = 248;

    public static final int BAD_INPUT = 302;
    public static int UNACCEPTABLE_ITEM = 304;
    public static int DUPLICATE_ITEM = 306;
    public static int BAD_REQUEST = 312;
    public static int CAPTCHA_INVALID = 316;
    public static int RATE_LIMIT_EXCEEDED = 318;
    public static int MISSING_DEPENDENCY = 326;
    public static int REQUEST_OR_FILE_TOO_LARGE = 330;
    public static int UNACCEPTABLE_FILE_TYPE = 332;

    public static int THIRD_PARTY_UNAVAILABLE = 408;
}
