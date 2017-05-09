package org.zalando.putittorest;

import com.google.common.annotations.VisibleForTesting;
import org.zalando.riptide.Plugin;
import org.zalando.riptide.RequestArguments;
import org.zalando.riptide.RequestExecution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

final class DeferredPlugin<P extends Plugin> implements Plugin {

    // turns out this is actually easier to use than AtomicReference since we want to use a lazy set-once
    private final ConcurrentMap<Plugin, P> delegate = new ConcurrentHashMap<>();

    private final Class<P> type;
    private final Supplier<P> loader;
    private final Supplier<P> creator;

    DeferredPlugin(final Class<P> type, final Supplier<P> loader, final Supplier<P> creator) {
        this.type = type;
        this.loader = loader;
        this.creator = creator;
    }

    @Override
    public RequestExecution prepare(@Nonnull final RequestArguments arguments,
            @Nonnull final RequestExecution execution) {
        return getDelegate().prepare(arguments, execution);
    }

    private P getDelegate() {
        return delegate.computeIfAbsent(this, $ -> {
            @Nullable final P loaded = loader.get();
            return loaded == null ? creator.get() : loaded;
        });
    }

    @VisibleForTesting
    Class<P> getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.getName() + "@" + Integer.toHexString(hashCode());
    }

}
