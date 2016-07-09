package org.zalando.putittorest;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;

class HttpAsyncClientFactoryBean implements FactoryBean<HttpAsyncClient> {

    private final HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create();

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
    public HttpAsyncClient getObject() {
        // TODO: builder.setConnectionTimeToLive(30, TimeUnit.SECONDS);
        return builder.build();
    }

    @Override
    public Class<?> getObjectType() {
        return HttpAsyncClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
