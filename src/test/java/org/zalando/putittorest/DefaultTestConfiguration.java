package org.zalando.putittorest;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.zalando.stups.tokens.AccessTokens;

import static org.mockito.Mockito.mock;

@Configuration
@ImportAutoConfiguration({RestClientAutoConfiguration.class, JacksonAutoConfiguration.class})
@ActiveProfiles("default")
public class DefaultTestConfiguration {

    @Bean
    public AccessTokens accessTokens() {
        return mock(AccessTokens.class);
    }

}
