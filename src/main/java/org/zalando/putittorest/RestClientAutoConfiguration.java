package org.zalando.putittorest;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.zalando.logbook.spring.LogbookAutoConfiguration;
import org.zalando.logbook.spring.LogbookHttpClientAutoConfiguration;
import org.zalando.logbook.spring.LogbookSecurityAutoConfiguration;
import org.zalando.tracer.spring.TracerAutoConfiguration;

@Configuration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@AutoConfigureAfter(value = {
        JacksonAutoConfiguration.class,
        LogbookAutoConfiguration.class,
        LogbookHttpClientAutoConfiguration.class,
        LogbookSecurityAutoConfiguration.class,
        TracerAutoConfiguration.class,
}, name = {
        "ZmonMetricFilterAutoConfiguration"
})
public class RestClientAutoConfiguration {

    @Bean
    public RestClientPostProcessor restClientPostProcessor(final PluginResolver resolver) {
        return new RestClientPostProcessor(resolver);
    }

    @Bean
    @ConditionalOnMissingBean(PluginResolver.class)
    public PluginResolver pluginResolver(final ListableBeanFactory factory) {
        return new DefaultPluginResolver(factory);
    }

}
