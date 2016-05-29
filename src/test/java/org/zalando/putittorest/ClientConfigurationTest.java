package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DefaultTestConfiguration.class)
@TestPropertySource(properties = {
        "rest.clients.example.timeouts.connect: 2",
        "rest.clients.example.timeouts.read: 3",
})
public final class ClientConfigurationTest {

    @Test
    public void shouldUseTimeouts() {
        // TODO implement
    }

}
