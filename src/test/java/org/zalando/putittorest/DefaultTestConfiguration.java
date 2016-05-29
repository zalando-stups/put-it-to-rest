package org.zalando.putittorest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.tokens.AccessTokens;

import static org.mockito.Mockito.mock;

@Configuration
@ImportAutoConfiguration(RestClientAutoConfiguration.class)
public class DefaultTestConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return mock(ObjectMapper.class);
    }

    @Bean
    public AccessTokens accessTokens() {
        return mock(AccessTokens.class);
    }

}
