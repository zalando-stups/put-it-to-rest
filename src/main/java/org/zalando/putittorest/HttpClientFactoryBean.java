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
