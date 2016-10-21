package com.yanming.plugin;

/**
 * Created by allan on 16/10/18.
 */
public interface AuthenticationPlugin {
    String getPluginName();

    byte[] process(byte[] password, byte[] seedAsBytes);
}
