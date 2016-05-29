package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.tracer.spring.TracerAutoConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration({
        DefaultTestConfiguration.class,
        TracerAutoConfiguration.class
})
public final class TracerCompatibilityTest {

    @Test
    public void shouldUseInterceptor() {
        // TODO implement
    }

}
