package org.zalando.putittorest;

import org.springframework.beans.factory.ListableBeanFactory;
import org.zalando.riptide.OriginalStackTracePlugin;
import org.zalando.riptide.Plugin;
import org.zalando.riptide.exceptions.TemporaryExceptionPlugin;
import org.zalando.riptide.hystrix.HystrixPlugin;

import java.util.function.Supplier;

public final class DefaultPluginResolver implements PluginResolver {

    private final ListableBeanFactory factory;

    public DefaultPluginResolver(final ListableBeanFactory factory) {
        this.factory = factory;
    }

    @Override
    public Plugin resolve(final String name) {
        switch (name) {
            case "original-stack-trace":
                return new OriginalStackTracePlugin();
            case "temporary-exception":
                return loadPlugin(factory, TemporaryExceptionPlugin.class,
                        TemporaryExceptionPlugin::new);
            case "hystrix":
                return loadPlugin(factory, HystrixPlugin.class,
                        HystrixPlugin::new);
            default:
                throw new IllegalArgumentException("Unknown plugin name: " + name);
        }
    }

    private <P extends Plugin> P loadPlugin(final ListableBeanFactory factory, final Class<P> type,
            final Supplier<P> creator) {
        if (factory.getBeanNamesForType(type).length > 0) {
            return factory.getBean(type);
        }
        return creator.get();
    }

}
