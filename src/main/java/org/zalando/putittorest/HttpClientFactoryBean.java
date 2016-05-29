package org.zalando.putittorest;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

class HttpClientFactoryBean implements FactoryBean<HttpClient> {

    private final HttpClientBuilder builder = HttpClientBuilder.create();

    public void setFirstRequestInterceptors(final List<HttpRequestInterceptor> interceptors) {
        interceptors.forEach(builder::addInterceptorFirst);
    }

    public void setLastRequestInterceptors(final List<HttpRequestInterceptor> interceptors) {
        interceptors.forEach(builder::addInterceptorLast);
    }

    public void setLastResponseInterceptors(final List<HttpResponseInterceptor> interceptors) {
        interceptors.forEach(builder::addInterceptorLast);
    }

    @Override
    public HttpClient getObject() {
        builder.setConnectionTimeToLive(30, TimeUnit.SECONDS); // TODO make configurable?
        return builder.build();
    }

    @Override
    public Class<?> getObjectType() {
        return HttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
