package org.zalando.putittorest;

/*
 * ⁣​
 * Put it to REST!
 * ⁣⁣
 * Copyright (C) 2015 - 2016 Zalando SE
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ​⁣
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.spring.LogbookAutoConfiguration;
import org.zalando.riptide.AsyncRest;
import org.zalando.riptide.Rest;
import org.zalando.tracer.spring.TracerAutoConfiguration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
public final class RestClientAutoConfigurationTest {

    @Rule
    public final EnvironmentVariables environment = new EnvironmentVariables();

    private static final RestTemplate TEMPLATE = new RestTemplate();

    @Configuration
    @ImportAutoConfiguration({
            TracerAutoConfiguration.class,
            LogbookAutoConfiguration.class,
            RestClientAutoConfiguration.class,
    })
    public static class TestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public RestTemplate businessPartnerRestTemplate() {
            return TEMPLATE;
        }

    }

    @Autowired
    @Qualifier("businessPartnerRestTemplate")
    private RestTemplate businessPartner;

    @Autowired
    @Qualifier("exchange-rate")
    private AsyncRest exchangeRate;

    @Autowired
    @Qualifier("ecb")
    private Rest ecb;

    public RestClientAutoConfigurationTest() {
        environment.set("ACCESS_TOKEN_URL", "http://example.com");
    }

    @Test
    public void shouldOverride() {
        assertThat(businessPartner, is(sameInstance(TEMPLATE)));
    }

}
