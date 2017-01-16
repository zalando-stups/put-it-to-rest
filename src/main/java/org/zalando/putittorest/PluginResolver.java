package org.zalando.putittorest;

import org.zalando.riptide.Plugin;

@FunctionalInterface
public interface PluginResolver {

    Plugin resolve(final String name);

}
