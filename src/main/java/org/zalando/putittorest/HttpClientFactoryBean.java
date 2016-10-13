package org.zalando.putittorest;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.getDefaultHostnameVerifier;

@Component
class HttpClientFactoryBean implements FactoryBean<HttpClient> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientFactoryBean.class);

    private final HttpClientBuilder builder = HttpClientBuilder.create();
    private final RequestConfig.Builder config = RequestConfig.custom();
    private HttpClientCustomizer customizer = $ -> {
    };

    public void setFirstRequestInterceptors(final List<HttpRequestInterceptor> interceptors) {
        interceptors.forEach(builder::addInterceptorFirst);
    }

    public void setLastRequestInterceptors(final List<HttpRequestInterceptor> interceptors) {
        interceptors.forEach(builder::addInterceptorLast);
    }

    public void setLastResponseInterceptors(final List<HttpResponseInterceptor> interceptors) {
        interceptors.forEach(builder::addInterceptorLast);
    }

    public void setConnectTimeout(final int connectTimeout) {
        config.setConnectTimeout(connectTimeout);
    }

    public void setSocketTimeout(final int socketTimeout) {
        config.setSocketTimeout(socketTimeout);
    }

    public void setTrustedKeystore(final Keystore keystore) throws Exception {
        final SSLContextBuilder contextBuilder = SSLContexts.custom();

        final String path = keystore.getPath();
        final String password = keystore.getPassword();

        final URL resource = HttpClientFactoryBean.class.getClassLoader().getResource(path);
        if (resource == null) {
            throw new FileNotFoundException(format("Keystore [%s] not found.", path));
        }

        try {
            contextBuilder.loadTrustMaterial(resource, password == null ? null : password.toCharArray());
            builder.setSSLSocketFactory(new SSLConnectionSocketFactory(contextBuilder.build(), getDefaultHostnameVerifier()));
        } catch (final Exception e) {
            LOG.error("Error loading keystore [{}]:", path, e);    // log full exception, bean initialization code swallows it
            throw e;
        }
    }

    public void setCustomizer(final HttpClientCustomizer customizer) {
        this.customizer = customizer;
    }

    @Override
    public CloseableHttpClient getObject() {
        builder.setDefaultRequestConfig(config.build());
        builder.setConnectionTimeToLive(30, TimeUnit.SECONDS);
        customizer.customize(builder);
        return builder.build();
    }

    @Override
    public Class<?> getObjectType() {
        return CloseableHttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
