package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.putittorest.annotation.RestClient;
import org.zalando.riptide.Rest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DefaultTestConfiguration.class)
public class RestClientAnnotationConfigurationTest {

    @RestClient("example")
    private Rest exampleRest;

    @RestClient("ecb")
    private Rest ecbRest;

    @Test
    public void shouldWireOAuthCorrectly() {
        assertThat(exampleRest, is(notNullValue()));
    }

    @Test
    public void shouldWireNonOAuthCorrectly() {
        assertThat(ecbRest, is(notNullValue()));
    }
}
