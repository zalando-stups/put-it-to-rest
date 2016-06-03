package org.zalando.putittorest;

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
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.context.EnvironmentAware;
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

public class RestClientPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

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

            final String templateId = Registry.generateBeanName(id, RestTemplate.class);

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
            final BeanDefinitionBuilder factory = BeanDefinitionBuilder.genericBeanDefinition(HttpComponentsClientHttpRequestFactory.class);
            factory.addConstructorArgReference(registerHttpClient(id));
            configureTimeouts(factory, timeouts);
            return factory;
        });
    }

    private String registerHttpClient(final String id) {
        return registry.register(id, HttpClient.class, () -> {
            final BeanDefinitionBuilder httpClient = BeanDefinitionBuilder.genericBeanDefinition(HttpClientFactoryBean.class);
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
                BeanDefinitionBuilder.genericBeanDefinition(HttpMessageConverters.class).
                        addConstructorArgValue(Registry.list(
                                BeanDefinitionBuilder.genericBeanDefinition(StringHttpMessageConverter.class)
                                        .addPropertyValue("writeAcceptCharset", false)
                                        .getBeanDefinition(),
                                BeanDefinitionBuilder.genericBeanDefinition(MappingJackson2HttpMessageConverter.class)
                                        .addConstructorArgReference(findObjectMapper(id))
                                        .getBeanDefinition())));
    }

    private String findObjectMapper(final String id) {
        final String beanName = Registry.generateBeanName(id, ObjectMapper.class);
        return registry.isRegistered(beanName) ? beanName : "objectMapper";
    }

    private String registerRestTemplate(final String id, final Client client,
            final String httpClientFactoryId, final String httpMessageConvertersId) {
        return registry.register(id, RestTemplate.class, () -> {
            final BeanDefinitionBuilder template = BeanDefinitionBuilder.genericBeanDefinition(RestTemplate.class);
            template.addConstructorArgReference(httpClientFactoryId);
            configureRestTemplate(template, client, httpMessageConvertersId);
            return template;
        });
    }

    private String registerStupsOAuth2RestTemplate(final RestSettings settings,
            final String id, final Client client, final String httpClientFactoryId,
            final String httpMessageConvertersId) {
        return registry.register(id, RestTemplate.class, () -> {
            final BeanDefinitionBuilder template = BeanDefinitionBuilder.genericBeanDefinition(StupsOAuth2RestTemplate.class);
            template.addConstructorArgValue(BeanDefinitionBuilder.genericBeanDefinition(StupsTokensAccessTokenProvider.class)
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

        final AbstractBeanDefinition converters = BeanDefinitionBuilder.genericBeanDefinition()
                .setFactoryMethod("getConverters")
                .getBeanDefinition();
        converters.setFactoryBeanName(messageConvertersId);

        builder.addPropertyValue("messageConverters", converters);
    }

    private String registerAccessTokens(final RestSettings settings) {
        return registry.register(AccessTokens.class, () -> {
            final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(AccessTokensFactoryBean.class);
            builder.addPropertyValue("settings", settings);
            return builder;
        });
    }

    private String registerRest(final String id, final String restTemplateId) {
        return registry.register(id, Rest.class, () -> {
            final BeanDefinitionBuilder rest = BeanDefinitionBuilder.genericBeanDefinition(Rest.class);
            rest.setFactoryMethod("create");
            rest.addConstructorArgReference(restTemplateId);
            return rest;
        });
    }

    private String registerAsyncRest(final String id, final Timeouts timeouts,
            final String restTemplateId) {
        return registry.register(id, AsyncRest.class, () -> {
            final BeanDefinitionBuilder rest = BeanDefinitionBuilder.genericBeanDefinition(AsyncRest.class);
            rest.setFactoryMethod("create");
            rest.addConstructorArgReference(registerAsyncRestTemplate(id, timeouts, restTemplateId));
            return rest;
        });
    }

    private String registerAsyncRestTemplate(final String id,
            final Timeouts timeouts, final String restTemplateId) {
        return registry.register(id, AsyncRestTemplate.class, () -> {
            final BeanDefinitionBuilder template = BeanDefinitionBuilder.genericBeanDefinition(AsyncRestTemplate.class);
            template.addConstructorArgReference(registerAsyncClientHttpRequestFactory(id, timeouts));
            template.addConstructorArgReference(restTemplateId);
            return template;
        });
    }

    private String registerAsyncClientHttpRequestFactory(final String id,
            final Timeouts timeouts) {
        return registry.register(id, AsyncClientHttpRequestFactory.class, () -> {
            final BeanDefinitionBuilder factory =
                    BeanDefinitionBuilder.genericBeanDefinition(HttpComponentsAsyncClientHttpRequestFactory.class);
            factory.addConstructorArgReference(registerHttpAsyncClient(id));
            configureTimeouts(factory, timeouts);
            return factory;
        });
    }

    private String registerHttpAsyncClient(final String id) {
        return registry.register(id, HttpAsyncClient.class, () -> {
            final BeanDefinitionBuilder httpClient = BeanDefinitionBuilder.genericBeanDefinition(HttpAsyncClientFactoryBean.class);
            configureInterceptors(httpClient);
            return httpClient;
        });
    }

    @Hack("In order to avoid a runtime dependency on Tracer and Logbook")
    private void configureInterceptors(final BeanDefinitionBuilder builder) {
        if (registry.isRegistered("tracerHttpRequestInterceptor")) {
            builder.addPropertyValue("firstRequestInterceptors", Registry.list(Registry.ref("tracerHttpRequestInterceptor")));
        }

        if (registry.isRegistered("logbookHttpRequestInterceptor")) {
            builder.addPropertyValue("lastRequestInterceptors", Registry.list(Registry.ref("logbookHttpRequestInterceptor")));
        }

        if (registry.isRegistered("logbookHttpResponseInterceptor")) {
            builder.addPropertyValue("lastResponseInterceptors", Registry.list(Registry.ref("logbookHttpResponseInterceptor")));
        }
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // nothing to do
    }

}