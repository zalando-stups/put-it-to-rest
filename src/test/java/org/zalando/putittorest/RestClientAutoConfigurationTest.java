package org.zalando.putittorest;

/*
 * ⁣​
 * Put it to REST!
 * ⁣⁣
 * Copyright (C) 2015 - 2016 Zalando SE
 * ⁣⁣
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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