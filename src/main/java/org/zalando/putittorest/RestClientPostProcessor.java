package org.zalando.putittorest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.zalando.putittorest.zmon.ZmonRequestInterceptor;
import org.zalando.putittorest.zmon.ZmonResponseInterceptor;
import org.zalando.riptide.Rest;
import org.zalando.riptide.httpclient.RestAsyncClientHttpRequestFactory;
import org.zalando.riptide.stream.Streams;
import org.zalando.stups.oauth2.httpcomponents.AccessTokensRequestInterceptor;
import org.zalando.stups.tokens.AccessTokens;
import org.zalando.tracer.concurrent.TracingExecutors;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.zalando.putittorest.Registry.generateBeanName;
import static org.zalando.putittorest.Registry.list;
import static org.zalando.putittorest.Registry.ref;

public class RestClientPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(RestClientPostProcessor.class);

    private ConfigurableEnvironment environment;
    private Registry registry;
    private RestSettings settings;

    @Override
    public void setEnvironment(final Environment environment) {
        // TODO under which circumstances can this be something else?
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry beanDefinitionRegistry) {
        this.registry = new Registry(beanDefinitionRegistry);

        getSettings().getClients().forEach((id, client) -> {
            final String factoryId = registerAsyncClientHttpRequestFactory(id, client);
            final String convertersId = registerHttpMessageConverters(id);
            final String baseUrl = client.getBaseUrl();

            registerRest(id, factoryId, convertersId, baseUrl);
            registerRestTemplate(id, factoryId, convertersId, baseUrl);
            registerAsyncRestTemplate(id, factoryId, convertersId, baseUrl);
        });
    }

    @VisibleForTesting
    @SneakyThrows
    RestSettings getSettings() {
        if (settings == null) {
            final PropertiesConfigurationFactory<RestSettings> factory =
                    new PropertiesConfigurationFactory<>(RestSettings.class);

            factory.setTargetName("rest");
            factory.setPropertySources(environment.getPropertySources());
            factory.setConversionService(environment.getConversionService());

            settings = factory.getObject();
        }
        return settings;
    }

    private String registerHttpMessageConverters(final String id) {
        return registry.register(id, HttpMessageConverters.class, () -> {
            final List<Object> list = list();

            LOG.debug("Client [{}]: Registering StringHttpMessageConverter", id);
            list.add(genericBeanDefinition(StringHttpMessageConverter.class)
                    .addPropertyValue("writeAcceptCharset", false)
                    .getBeanDefinition());

            final String objectMapperId = findObjectMapper(id);

            LOG.debug("Client [{}]: Registering MappingJackson2HttpMessageConverter referencing [{}]", id, objectMapperId);
            list.add(genericBeanDefinition(MappingJackson2HttpMessageConverter.class)
                    .addConstructorArgReference(objectMapperId)
                    .getBeanDefinition());

            LOG.debug("Client [{}]: Registering StreamConverter referencing [{}]", id, objectMapperId);
            list.add(genericBeanDefinition(Streams.class)
                    .setFactoryMethod("streamConverter")
                    .addConstructorArgReference(objectMapperId)
                    .getBeanDefinition());

            return BeanDefinitionBuilder.genericBeanDefinition(ClientHttpMessageConverters.class)
                    .addConstructorArgValue(list);
        });
    }

    private String findObjectMapper(final String id) {
        final String beanName = Registry.generateBeanName(id, ObjectMapper.class);
        return registry.isRegistered(beanName) ? beanName : "jacksonObjectMapper";
    }

    private String registerAccessTokens(final String id, final RestSettings settings) {
        return registry.register(AccessTokens.class, () -> {
            LOG.debug("Client [{}]: Registering AccessTokens", id);
            final BeanDefinitionBuilder builder = genericBeanDefinition(AccessTokensFactoryBean.class);
            builder.addPropertyValue("settings", settings);
            return builder;
        });
    }

    private String registerRest(final String id, final String factoryId, final String convertersId,
            @Nullable final String baseUrl) {
        return registry.register(id, Rest.class, () -> {
            LOG.debug("Client [{}]: Registering Rest", id);

            final BeanDefinitionBuilder rest = genericBeanDefinition(RestFactory.class);
            rest.setFactoryMethod("create");
            rest.addConstructorArgReference(factoryId);

            final AbstractBeanDefinition converters = BeanDefinitionBuilder.genericBeanDefinition()
                    .setFactoryMethod("getConverters")
                    .getBeanDefinition();
            converters.setFactoryBeanName(convertersId);
            rest.addConstructorArgValue(converters);

            rest.addConstructorArgValue(baseUrl);
            return rest;
        });
    }

    private String registerRestTemplate(final String id, final String factoryId, final String convertersId,
            @Nullable final String baseUrl) {
        return registry.register(id, RestTemplate.class, () -> {
            LOG.debug("Client [{}]: Registering RestTemplate", id);

            final BeanDefinitionBuilder restTemplate = genericBeanDefinition(RestTemplate.class);

            restTemplate.addConstructorArgReference(factoryId);

            final DefaultUriTemplateHandler handler = new DefaultUriTemplateHandler();
            handler.setBaseUrl(baseUrl);
            restTemplate.addPropertyValue("uriTemplateHandler", handler);

            final AbstractBeanDefinition converters = BeanDefinitionBuilder.genericBeanDefinition()
                    .setFactoryMethod("getConverters")
                    .getBeanDefinition();
            converters.setFactoryBeanName(convertersId);
            restTemplate.addPropertyValue("messageConverters", converters);

            return restTemplate;
        });
    }

    private String registerAsyncRestTemplate(final String id, final String factoryId, final String convertersId,
            @Nullable final String baseUrl) {
        return registry.register(id, AsyncRestTemplate.class, () -> {
            LOG.debug("Client [{}]: Registering AsyncRestTemplate", id);

            final BeanDefinitionBuilder restTemplate = genericBeanDefinition(AsyncRestTemplate.class);

            restTemplate.addConstructorArgReference(factoryId);

            final DefaultUriTemplateHandler handler = new DefaultUriTemplateHandler();
            handler.setBaseUrl(baseUrl);
            restTemplate.addPropertyValue("uriTemplateHandler", handler);

            final AbstractBeanDefinition converters = BeanDefinitionBuilder.genericBeanDefinition()
                    .setFactoryMethod("getConverters")
                    .getBeanDefinition();
            converters.setFactoryBeanName(convertersId);
            restTemplate.addPropertyValue("messageConverters", converters);

            return restTemplate;
        });
    }

    private String registerAsyncClientHttpRequestFactory(final String id, final Client client) {
        return registry.register(id, AsyncClientHttpRequestFactory.class, () -> {
            LOG.debug("Client [{}]: Registering RestAsyncClientHttpRequestFactory", id);

            final BeanDefinitionBuilder factory =
                    genericBeanDefinition(RestAsyncClientHttpRequestFactory.class);

            factory.addConstructorArgReference(registerHttpClient(id, client));
            factory.addConstructorArgValue(genericBeanDefinition(ConcurrentTaskExecutor.class)
                    .addConstructorArgValue(BeanDefinitionBuilder.genericBeanDefinition(TracingExecutors.class)
                            .setFactoryMethod("preserve")
                            .addConstructorArgValue(genericBeanDefinition(Executors.class)
                                    .setFactoryMethod("newCachedThreadPool")
                                    .setDestroyMethodName("shutdown")
                                    .getBeanDefinition())
                            .addConstructorArgReference("tracer")
                            .getBeanDefinition())
                    .getBeanDefinition());

            return factory;
        });
    }

    private String registerHttpClient(final String id, final Client client) {
        return registry.register(id, HttpClient.class, () -> {
            LOG.debug("Client [{}]: Registering HttpClient", id);

            final BeanDefinitionBuilder httpClient = genericBeanDefinition(HttpClientFactoryBean.class);
            configureTimeouts(httpClient, id, client.getTimeouts());
            configureInterceptors(httpClient, id, client.getOauth());

            final String customizerId = generateBeanName(id, HttpClientCustomizer.class);
            if (registry.isRegistered(customizerId)) {
                LOG.debug("Client [{}]: Customizing HttpClient with [{}]", id, customizerId);
                httpClient.addPropertyReference("customizer", customizerId);
            }

            return httpClient;
        });
    }

    private void configureTimeouts(final BeanDefinitionBuilder builder, final String id, final Timeouts timeouts) {
        final int connect = timeouts.getConnect();
        final TimeUnit connectUnit = timeouts.getConnectUnit();
        final TimeUnit readUnit = timeouts.getReadUnit();
        final int read = timeouts.getRead();
        
        LOG.debug("Client [{}]: Configuring connect timeout: [{} {}]", id, connect, connectUnit);
        builder.addPropertyValue("connectTimeout", (int) connectUnit.toMillis(connect));
        LOG.debug("Client [{}]: Configuring socket timeout: [{} {}]", id, read, readUnit);
        builder.addPropertyValue("socketTimeout", (int) readUnit.toMillis(read));
    }

    private void configureInterceptors(final BeanDefinitionBuilder builder, final String id,
            @Nullable final OAuth oauth) {
        final List<Object> requestInterceptors = list();
        final List<Object> responseInterceptors = list();

        if (oauth != null) {
            LOG.debug("Client [{}]: Registering AccessTokensRequestInterceptor", id);
            requestInterceptors.add(genericBeanDefinition(AccessTokensRequestInterceptor.class)
                    .addConstructorArgValue(id)
                    .addConstructorArgReference(registerAccessTokens(id, getSettings()))
                    .getBeanDefinition());
        }

        LOG.debug("Client [{}]: Registering TracerHttpRequestInterceptor", id);
        requestInterceptors.add(ref("tracerHttpRequestInterceptor"));

        if (registry.isRegistered("zmonMetricsWrapper")) {
            LOG.debug("Client [{}]: Registering ZmonRequestInterceptor", id);
            requestInterceptors.add(genericBeanDefinition(ZmonRequestInterceptor.class).getBeanDefinition());
            LOG.debug("Client [{}]: Registering ZmonResponseInterceptor", id);
            responseInterceptors.add(genericBeanDefinition(ZmonResponseInterceptor.class)
                    .addConstructorArgValue(ref("zmonMetricsWrapper"))
                    .getBeanDefinition());
        }

        LOG.debug("Client [{}]: Registering LogbookHttpResponseInterceptor", id);
        responseInterceptors.add(ref("logbookHttpResponseInterceptor"));

        LOG.debug("Client [{}]: Registering LogbookHttpRequestInterceptor", id);
        final List<Object> lastRequestInterceptors = list(ref("logbookHttpRequestInterceptor"));

        builder.addPropertyValue("firstRequestInterceptors", requestInterceptors);
        builder.addPropertyValue("lastRequestInterceptors", lastRequestInterceptors);
        builder.addPropertyValue("lastResponseInterceptors", responseInterceptors);
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // nothing to do
    }

}