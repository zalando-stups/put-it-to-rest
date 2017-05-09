package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.riptide.OriginalStackTracePlugin;
import org.zalando.riptide.Plugin;
import org.zalando.riptide.Rest;
import org.zalando.riptide.exceptions.TemporaryExceptionPlugin;
import org.zalando.riptide.hystrix.HystrixPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Component
public final class PluginTest {

    @Configuration
    @Import(DefaultTestConfiguration.class)
    public static class TestConfiguration {

    }

    @Autowired
    @Qualifier("example")
    private Rest example;

    @Autowired
    @Qualifier("ecb")
    private Rest ecb;

    @Autowired
    @Qualifier("github")
    private Rest github;

    @Test
    public void shouldUseDefault() throws Exception {
        assertThat(getPlugins(example), contains(equalTo(TemporaryExceptionPlugin.class)));
    }

    @Test
    public void shouldUseTemporaryException() throws Exception {
        assertThat(getPlugins(ecb), contains(equalTo(OriginalStackTracePlugin.class)));
    }

    @Test
    public void emptyListOfPluginsShouldUseDefaults() throws Exception {
        assertThat(getPlugins(github), contains(equalTo(TemporaryExceptionPlugin.class)));
    }

    private List<Class<? extends Plugin>> getPlugins(final Rest rest) throws Exception {
        final List<Class<? extends Plugin>> plugins = new ArrayList<>();

        final Field field = Rest.class.getDeclaredField("plugin");
        field.setAccessible(true);

        final Plugin plugin = (Plugin) field.get(rest);

        plugins.add(plugin instanceof DeferredPlugin ?
                DeferredPlugin.class.cast(plugin).getType() :
                plugin.getClass());

        return plugins;
    }

}
