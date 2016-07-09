package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.putittorest.annotation.RestClient;
import org.zalando.riptide.Rest;
import org.zalando.tracer.spring.TracerAutoConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration({
        DefaultTestConfiguration.class,
        TracerAutoConfiguration.class
})
public final class TracerCompatibilityTest {

    @RestClient("example")
    private Rest rest;

    @Test
    public void shouldUseInterceptor() {
        assertThat(rest, is(notNullValue()));
        // TODO implement
    }

}
