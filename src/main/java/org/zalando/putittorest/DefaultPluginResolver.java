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

    private final ImmutableMap<String, Supplier<Plugin>> plugins;

    public DefaultPluginResolver(final ListableBeanFactory factory) {
        this.plugins = ImmutableMap.of(
                "original-stack-trace", OriginalStackTracePlugin::new,
                "temporary-exception", () ->
                        load(factory, TemporaryExceptionPlugin.class, TemporaryExceptionPlugin::new),
                "hystrix", () ->
                        load(factory, HystrixPlugin.class, HystrixPlugin::new)
        );
    }

    private static <P extends Plugin> P load(final ListableBeanFactory factory, final Class<P> type,
            final Supplier<P> creator) {

        if (factory.getBeanNamesForType(type).length > 0) {
            return factory.getBean(type);
        }
        return creator.get();
    }

    @Override
    public Plugin resolve(final String name) {
        @Nullable final Supplier<Plugin> provider = plugins.get(name);

        if (provider == null) {
            throw new IllegalArgumentException("Unknown plugin name: " + name);
        }

        return provider.get();
    }

}
