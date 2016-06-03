package org.zalando.putittorest;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class RestClientAutoConfiguration {

    @Bean
    public static RestClientPostProcessor restClientPostProcessor() {
        return new RestClientPostProcessor();
    }

}
