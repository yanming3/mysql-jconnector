package com.yanming.response;

/**
 * Created by allan on 16/10/19.
 */
public class ServerGreeting {
    private final String pluginName;
    private final byte[] seed;
    private int serverCapabilities;

    public ServerGreeting(int serverCapabilities, String pluginName, byte[] seed) {
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
