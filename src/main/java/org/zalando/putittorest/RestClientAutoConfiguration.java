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


import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.zalando.riptide.AsyncRest;
import org.zalando.riptide.PassThroughResponseErrorHandler;
import org.zalando.riptide.Rest;
import org.zalando.stups.oauth2.spring.client.StupsOAuth2RestTemplate;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.tokens.AccessTokens;
import org.zalando.stups.tokens.AccessTokensBuilder;
import org.zalando.stups.tokens.Tokens;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

@Configuration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class RestClientAutoConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void registerBeanDefinitions(final AnnotationMetadata metadata, final BeanDefinitionRegistry registry) {
        final RestSettings settings = getSettings();
        final AccessTokens tokens = createAccessTokens(settings);

        settings.getClients().forEach((id, client) -> {
            final OAuth oauth = client.getOauth().merge(settings.getOauth());
            final Timeouts timeouts = client.getTimeouts();

            final String httpClientId = register(registry, id, HttpClient.class, () -> {
                final BeanDefinitionBuilder httpClient = genericBeanDefinition(HttpClientFactoryBean.class);
                configureInterceptors(httpClient);
                return httpClient;
            });

            final String httpClientFactoryId = register(registry, id, ClientHttpRequestFactory.class, () -> {
                final BeanDefinitionBuilder factory =
                        genericBeanDefinition(HttpComponentsClientHttpRequestFactory.class);
                factory.addConstructorArgReference(httpClientId);
                configureTimeouts(factory, timeouts);
                return factory;
            });

            final String restTemplateId;

            if (oauth.isEnabled()) {
                restTemplateId = register(registry, id, RestTemplate.class, () -> {
                    final BeanDefinitionBuilder template = genericBeanDefinition(StupsOAuth2RestTemplate.class);
                    template.addConstructorArgValue(new StupsTokensAccessTokenProvider(id, tokens));
                    template.addConstructorArgReference(httpClientFactoryId);
                    configureRestTemplate(registry, template, id, client);
                    return template;
                });
            } else {
                restTemplateId = register(registry, id, RestTemplate.class, () -> {
                    final BeanDefinitionBuilder template = genericBeanDefinition(RestTemplate.class);
                    template.addConstructorArgReference(httpClientFactoryId);
                    configureRestTemplate(registry, template, id, client);
                    return template;
                });
            }

            register(registry, id, Rest.class, () -> {
                final BeanDefinitionBuilder rest = genericBeanDefinition(Rest.class);
                rest.setFactoryMethod("create");
                rest.addConstructorArgReference(restTemplateId);
                return rest;
            });

            final String asyncHttpClientId = register(registry, id, HttpAsyncClient.class, () -> {
                final BeanDefinitionBuilder httpClient = genericBeanDefinition(HttpAsyncClientFactoryBean.class);
                configureInterceptors(httpClient);
                return httpClient;
            });

            final String asyncClientFactoryId = register(registry, id, AsyncClientHttpRequestFactory.class, () -> {
                final BeanDefinitionBuilder factory =
                        genericBeanDefinition(HttpComponentsAsyncClientHttpRequestFactory.class);
                factory.addConstructorArgReference(asyncHttpClientId);
                configureTimeouts(factory, timeouts);
                return factory;
            });

            final String asyncRestTemplateId = register(registry, id, AsyncRestTemplate.class, () -> {
                final BeanDefinitionBuilder template = genericBeanDefinition(AsyncRestTemplate.class);
                template.addConstructorArgReference(asyncClientFactoryId);
                template.addConstructorArgReference(restTemplateId);
                return template;
            });

            register(registry, id, AsyncRest.class, () -> {
                final BeanDefinitionBuilder rest = genericBeanDefinition(AsyncRest.class);
                rest.setFactoryMethod("create");
                rest.addConstructorArgReference(asyncRestTemplateId);
                return rest;
            });
        });
    }

    private void configureInterceptors(final BeanDefinitionBuilder builder) {
        builder.addPropertyValue("firstRequestInterceptors", list(ref("tracerHttpRequestInterceptor")));
        builder.addPropertyValue("lastRequestInterceptors", list(ref("logbookHttpRequestInterceptor")));
        builder.addPropertyValue("lastResponseInterceptors", list(ref("logbookHttpResponseInterceptor")));
    }

    private void configureTimeouts(BeanDefinitionBuilder builder, Timeouts timeouts) {
        builder.addPropertyValue("connectTimeout", secondsToMillis(timeouts.getConnect()));
        builder.addPropertyValue("readTimeout", secondsToMillis(timeouts.getRead()));
    }

    private void configureRestTemplate(final BeanDefinitionRegistry registry, final BeanDefinitionBuilder builder,
            final String id, final Client client) {
        builder.addPropertyValue("errorHandler", new PassThroughResponseErrorHandler());
        final DefaultUriTemplateHandler handler = new DefaultUriTemplateHandler();
        handler.setBaseUrl(client.getBaseUrl());
        builder.addPropertyValue("uriTemplateHandler", handler);

        final String jsonConverterId = register(registry, id, MappingJackson2HttpMessageConverter.class, () -> {
            final BeanDefinitionBuilder converter = genericBeanDefinition(MappingJackson2HttpMessageConverter.class);
            converter.addConstructorArgReference("objectMapper");
            return converter;
        });

        final StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setWriteAcceptCharset(false);

        builder.addPropertyValue("messageConverters", list(stringConverter, ref(jsonConverterId)));
    }

    private List<Object> list(final Object... elements) {
        final ManagedList<Object> list = new ManagedList<>();
        Collections.addAll(list, elements);
        return list;
    }

    private BeanReference ref(String beanName) {
        return new RuntimeBeanReference(beanName);
    }

    private AccessTokens createAccessTokens(final RestSettings settings) {
        final GlobalOAuth oauth = settings.getOauth();
        final Timeouts timeouts = oauth.getTimeouts();
        final AccessTokensBuilder builder = Tokens.createAccessTokensWithUri(oauth.getAccessTokenUrl())
                .schedulingPeriod(oauth.getSchedulingPeriod())
                .connectTimeout(secondsToMillis(timeouts.getConnect()))
                .socketTimeout(secondsToMillis(timeouts.getRead()));

        settings.getClients().forEach((id, client) ->
                builder.manageToken(id)
                        .addScopes(client.getOauth().getScopes())
                        .done());

        return builder.start();
    }

    private <T> String register(final BeanDefinitionRegistry registry, final String id, final Class<T> type,
            final Supplier<BeanDefinitionBuilder> factory) {

        final AbstractBeanDefinition definition = factory.get().getBeanDefinition();

        final AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(Qualifier.class);
        qualifier.setAttribute(AutowireCandidateQualifier.VALUE_KEY, id);
        definition.addQualifier(qualifier);

        final String name = beanName(id, type);

        if (!registry.isBeanNameInUse(name)) {
            registry.registerBeanDefinition(name, definition);
        }

        return name;
    }

    @SneakyThrows
    private RestSettings getSettings() {
        final PropertiesConfigurationFactory<RestSettings> factory =
                new PropertiesConfigurationFactory<>(RestSettings.class);

        factory.setTargetName("rest");
        factory.setPropertySources(environment.getPropertySources());
        factory.setConversionService(environment.getConversionService());

        return factory.getObject();
    }

    private <T> String beanName(final String id, final Class<T> type) {
        return id + type.getSimpleName();
    }

    private static int secondsToMillis(int value) {
        return (int) TimeUnit.SECONDS.toMillis(value);
    }

}
