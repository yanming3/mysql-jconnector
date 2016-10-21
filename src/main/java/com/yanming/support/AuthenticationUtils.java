package com.yanming.support;

import com.yanming.plugin.AuthenticationPlugin;
import com.yanming.plugin.MysqlNativePasswordPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by allan on 16/10/18.
 */
public class AuthenticationUtils {
    private static Map<String, AuthenticationPlugin> pluginsMap = new HashMap<>();

    static {
        AuthenticationPlugin nativePasswordPlugin = new MysqlNativePasswordPlugin();
        pluginsMap.put(nativePasswordPlugin.getPluginName(), nativePasswordPlugin);
    }

    public static AuthenticationPlugin getPlugin(String name) {
        return pluginsMap.get(name);
    }
}
