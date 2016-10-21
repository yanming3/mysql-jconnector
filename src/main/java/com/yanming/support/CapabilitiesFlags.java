package com.yanming.support;

/**
 * Created by allan on 16/10/18.
 */
public final class CapabilitiesFlags {
    public static final int CLIENT_PROTOCOL_41 = 0x00000200; // for > 4.1.1
    public static final int CLIENT_CONNECT_WITH_DB = 0x00000008;
    public static final int CLIENT_LONG_FLAG = 0x00000004; /* Get all column flags */
    public static final int CLIENT_DEPRECATE_EOF = 0x01000000;
    public static final int CLIENT_LONG_PASSWORD = 0x00000001; /* new more secure passwords */
    public static final int CLIENT_TRANSACTIONS = 0x00002000; // Client knows about transactions
    public static final int CLIENT_MULTI_RESULTS = 0x00020000; // Enable/disable multi-results
    public static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;

    public final static int CLIENT_PLUGIN_AUTH = 0x80000;
    public final static int CLIENT_SECURE_CONNECTION = 0x8000;
    public static final int CLIENT_FOUND_ROWS = 0x00000002;
    public  static final int CLIENT_CONNECT_ATTRS = 0x00100000;
}
