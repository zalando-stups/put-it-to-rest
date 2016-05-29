package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.logbook.spring.LogbookAutoConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DefaultTestConfiguration.class)
@TestPropertySource(properties = {
        "rest.oauth.scheduling-period: 15",
        "rest.oauth.timeouts.connect: 2",
        "rest.oauth.timeouts.read: 3",
})
public final class OAuthConfigurationTest {

    @Test
    public void shouldUseSchedulingPeriod() {
        // TODO implement
    }

    @Test
    public void shouldUseTimeouts() {
        // TODO implement
    }

}
