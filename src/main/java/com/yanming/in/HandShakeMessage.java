package com.yanming.in;

/**
 * Created by allan on 16/10/19.
 */
public class HandShakeMessage {
    private final String pluginName;
    private final byte[] seed;
    private int serverCapabilities;

    public HandShakeMessage(int serverCapabilities, String pluginName, byte[] seed) {
        this.serverCapabilities=serverCapabilities;
        this.pluginName = pluginName;
        this.seed = seed;
    }

    public String getPluginName() {
        return pluginName;
    }

    public byte[] getSeed() {
        return seed;
    }

    public int getServerCapabilities() {
        return serverCapabilities;
    }
}
