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
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.riptide.Rest;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
public final class RestClientAutoConfigurationTest {

    @Configuration
    @EnableAutoConfiguration
    public static class TestConfiguration {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public HttpRequestInterceptor tracerHttpRequestInterceptor() {
            return mock(HttpRequestInterceptor.class);
        }

        @Bean
        public HttpRequestInterceptor logbookHttpRequestInterceptor() {
            return mock(HttpRequestInterceptor.class);
        }

        @Bean
        public HttpResponseInterceptor logbookHttpResponseInterceptor() {
            return mock(HttpResponseInterceptor.class);
        }

    }

    @Autowired
    @Qualifier("ecb")
    private Rest ecb;

    @Autowired
    @Qualifier("business-partner")
    private Rest businessPartner;

    @Test
    public void shouldRun() {

    }

}