package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@TestPropertySource(properties = "rest.oauth.access-token-url: http://example.com")
public final class AccessTokensPropertiesTest {

    @Configuration
    @ImportAutoConfiguration({RestClientAutoConfiguration.class, JacksonAutoConfiguration.class})
    public static class TestConfiguration {

    }

    @Test
    public void shouldRun() {
        // if the application context is booting up, I'm happy
    }

}
