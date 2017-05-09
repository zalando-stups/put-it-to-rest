package org.zalando.putittorest;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.ListableBeanFactory;
import org.zalando.riptide.OriginalStackTracePlugin;
import org.zalando.riptide.Plugin;
import org.zalando.riptide.exceptions.TemporaryExceptionPlugin;
import org.zalando.riptide.hystrix.HystrixPlugin;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class DefaultPluginResolver implements PluginResolver {

    private final ImmutableMap<String, Plugin> plugins;

    public DefaultPluginResolver(final ListableBeanFactory factory) {
        this.plugins = ImmutableMap.of(
                "original-stack-trace", new OriginalStackTracePlugin(),
                "temporary-exception", load(factory, TemporaryExceptionPlugin.class, TemporaryExceptionPlugin::new),
                "hystrix", load(factory, HystrixPlugin.class, HystrixPlugin::new)
        );
    }

    private static <P extends Plugin> Plugin load(final ListableBeanFactory factory, final Class<P> type,
            final Supplier<P> creator) {

        final Supplier<P> loader = () -> factory.getBeanNamesForType(type).length > 0 ? factory.getBean(type) : null;
        return new DeferredPlugin<>(type, loader, creator);
    }

    @Override
    public Plugin resolve(final String name) {
        @Nullable final Plugin plugin = plugins.get(name);

        if (plugin == null) {
            throw new IllegalArgumentException("Unknown plugin name: " + name);
        }

        return plugin;
    }

}
