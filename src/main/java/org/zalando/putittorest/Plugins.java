package org.zalando.putittorest;

import org.zalando.riptide.Plugin;

import java.util.List;

public final class Plugins {

    private final List<Plugin> plugins;

    public Plugins(final List<Plugin> plugins) {
        this.plugins = plugins;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

}
