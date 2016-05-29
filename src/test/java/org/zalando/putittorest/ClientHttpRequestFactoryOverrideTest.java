package org.zalando.putittorest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.zalando.putittorest.Mocks.isMock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
public final class ClientHttpRequestFactoryOverrideTest {

    @Configuration
    @Import(DefaultTestConfiguration.class)
    public static class TestConfiguration {

        @Bean
        @Qualifier("example")
        public ClientHttpRequestFactory exampleClientHttpRequestFactory() {
            return mock(ClientHttpRequestFactory.class);
        }

    }

    @Autowired
    @Qualifier("example")
    // we had to be more specific, because there are two different factories
    private ClientHttpRequestFactory exampleClientHttpRequestFactory;

    @Test
    public void shouldOverride() {
        assertThat(exampleClientHttpRequestFactory, isMock());
    }

}
