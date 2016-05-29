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
import com.google.common.annotations.VisibleForTesting;
import com.google.gag.annotation.remark.Hack;
import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
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

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.zalando.putittorest.Registry.generateBeanName;
import static org.zalando.putittorest.Registry.list;
import static org.zalando.putittorest.Registry.ref;

@Configuration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class RestClientAutoConfiguration implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private ConfigurableEnvironment environment;
    private Registry registry;

    @Override
    public void setEnvironment(final Environment environment) {
        // TODO under which circumstances can this be something else?
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        final RestSettings settings = getSettings();
        this.registry = new Registry(beanDefinitionRegistry);

        settings.getClients().forEach((id, client) -> {
            @Nullable final OAuth oauth = client.getOauth();
            final Timeouts timeouts = client.getTimeouts();

            final String templateId = generateBeanName(id, RestTemplate.class);

            if (registry.isNotRegistered(templateId)) {
                final String factoryId = registerClientHttpRequestFactory(id, timeouts);
                final String convertersId = registerHttpMessageConverters(id);

                if (oauth == null) {
                    registerRestTemplate(id, client, factoryId, convertersId);
                } else {
                    registerStupsOAuth2RestTemplate(settings, id, client, factoryId, convertersId);
                }
            }

            registerRest(id, templateId);
            registerAsyncRest(id, timeouts, templateId);
        });
    }

    @VisibleForTesting
    @SneakyThrows
    RestSettings getSettings() {
        final PropertiesConfigurationFactory<RestSettings> factory =
                new PropertiesConfigurationFactory<>(RestSettings.class);

        factory.setTargetName("rest");
        factory.setPropertySources(environment.getPropertySources());
        factory.setConversionService(environment.getConversionService());

        return factory.getObject();
    }

    private String registerClientHttpRequestFactory(final String id,
            final Timeouts timeouts) {
        return registry.register(id, ClientHttpRequestFactory.class, () -> {
            final BeanDefinitionBuilder factory = genericBeanDefinition(HttpComponentsClientHttpRequestFactory.class);
            factory.addConstructorArgReference(registerHttpClient(id));
            configureTimeouts(factory, timeouts);
            return factory;
        });
    }

    private String registerHttpClient(final String id) {
        return registry.register(id, HttpClient.class, () -> {
            final BeanDefinitionBuilder httpClient = genericBeanDefinition(HttpClientFactoryBean.class);
            configureInterceptors(httpClient);
            return httpClient;
        });
    }

    private void configureTimeouts(final BeanDefinitionBuilder builder, final Timeouts timeouts) {
        builder.addPropertyValue("connectTimeout", (int) TimeUnit.SECONDS.toMillis(timeouts.getConnect()));
        builder.addPropertyValue("readTimeout", (int) TimeUnit.SECONDS.toMillis(timeouts.getRead()));
    }

    private String registerHttpMessageConverters(final String id) {
        return registry.register(id, HttpMessageConverters.class, () ->
                genericBeanDefinition(HttpMessageConverters.class).
                        addConstructorArgValue(list(
                                genericBeanDefinition(StringHttpMessageConverter.class)
                                        .addPropertyValue("writeAcceptCharset", false)
                                        .getBeanDefinition(),
                                genericBeanDefinition(MappingJackson2HttpMessageConverter.class)
                                        .addConstructorArgReference(findObjectMapper(id))
                                        .getBeanDefinition())));
    }

    private String findObjectMapper(final String id) {
        final String beanName = generateBeanName(id, ObjectMapper.class);
        return registry.isRegistered(beanName) ? beanName : "objectMapper";
    }

    private String registerRestTemplate(final String id, final Client client,
            final String httpClientFactoryId, final String httpMessageConvertersId) {
        return registry.register(id, RestTemplate.class, () -> {
            final BeanDefinitionBuilder template = genericBeanDefinition(RestTemplate.class);
            template.addConstructorArgReference(httpClientFactoryId);
            configureRestTemplate(template, client, httpMessageConvertersId);
            return template;
        });
    }

    private String registerStupsOAuth2RestTemplate(final RestSettings settings,
            final String id, final Client client, final String httpClientFactoryId,
            final String httpMessageConvertersId) {
        return registry.register(id, RestTemplate.class, () -> {
            final BeanDefinitionBuilder template = genericBeanDefinition(StupsOAuth2RestTemplate.class);
            template.addConstructorArgValue(genericBeanDefinition(StupsTokensAccessTokenProvider.class)
                    .addConstructorArgValue(id)
                    .addConstructorArgReference(registerAccessTokens(settings))
                    .getBeanDefinition());
            template.addConstructorArgReference(httpClientFactoryId);
            configureRestTemplate(template, client, httpMessageConvertersId);
            return template;
        });
    }

    private void configureRestTemplate(final BeanDefinitionBuilder builder, final Client client,
            final String messageConvertersId) {
        builder.addPropertyValue("errorHandler", new PassThroughResponseErrorHandler());
        final DefaultUriTemplateHandler handler = new DefaultUriTemplateHandler();
        handler.setBaseUrl(client.getBaseUrl());
        builder.addPropertyValue("uriTemplateHandler", handler);

        final AbstractBeanDefinition converters = genericBeanDefinition()
                .setFactoryMethod("getConverters")
                .getBeanDefinition();
        converters.setFactoryBeanName(messageConvertersId);

        builder.addPropertyValue("messageConverters", converters);
    }

    private String registerAccessTokens(final RestSettings settings) {
        return registry.register(AccessTokens.class, () -> {
            final BeanDefinitionBuilder builder = genericBeanDefinition(AccessTokensFactoryBean.class);
            builder.addPropertyValue("settings", settings);
            return builder;
        });
    }

    private String registerRest(final String id, final String restTemplateId) {
        return registry.register(id, Rest.class, () -> {
            final BeanDefinitionBuilder rest = genericBeanDefinition(Rest.class);
            rest.setFactoryMethod("create");
            rest.addConstructorArgReference(restTemplateId);
            return rest;
        });
    }

    private String registerAsyncRest(final String id, final Timeouts timeouts,
            final String restTemplateId) {
        return registry.register(id, AsyncRest.class, () -> {
            final BeanDefinitionBuilder rest = genericBeanDefinition(AsyncRest.class);
            rest.setFactoryMethod("create");
            rest.addConstructorArgReference(registerAsyncRestTemplate(id, timeouts, restTemplateId));
            return rest;
        });
    }

    private String registerAsyncRestTemplate(final String id,
            final Timeouts timeouts, final String restTemplateId) {
        return registry.register(id, AsyncRestTemplate.class, () -> {
            final BeanDefinitionBuilder template = genericBeanDefinition(AsyncRestTemplate.class);
            template.addConstructorArgReference(registerAsyncClientHttpRequestFactory(id, timeouts));
            template.addConstructorArgReference(restTemplateId);
            return template;
        });
    }

    private String registerAsyncClientHttpRequestFactory(final String id,
            final Timeouts timeouts) {
        return registry.register(id, AsyncClientHttpRequestFactory.class, () -> {
            final BeanDefinitionBuilder factory =
                    genericBeanDefinition(HttpComponentsAsyncClientHttpRequestFactory.class);
            factory.addConstructorArgReference(registerHttpAsyncClient(id));
            configureTimeouts(factory, timeouts);
            return factory;
        });
    }

    private String registerHttpAsyncClient(final String id) {
        return registry.register(id, HttpAsyncClient.class, () -> {
            final BeanDefinitionBuilder httpClient = genericBeanDefinition(HttpAsyncClientFactoryBean.class);
            configureInterceptors(httpClient);
            return httpClient;
        });
    }

    @Hack("In order to avoid a runtime dependency on Tracer and Logbook")
    private void configureInterceptors(final BeanDefinitionBuilder builder) {
        if (registry.isRegistered("tracerHttpRequestInterceptor")) {
            builder.addPropertyValue("firstRequestInterceptors", list(ref("tracerHttpRequestInterceptor")));
        }

        if (registry.isRegistered("logbookHttpRequestInterceptor")) {
            builder.addPropertyValue("lastRequestInterceptors", list(ref("logbookHttpRequestInterceptor")));
        }

        if (registry.isRegistered("logbookHttpResponseInterceptor")) {
            builder.addPropertyValue("lastResponseInterceptors", list(ref("logbookHttpResponseInterceptor")));
        }
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // nothing to do
    }

}
