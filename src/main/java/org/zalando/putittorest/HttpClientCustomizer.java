package org.zalando.putittorest;

import org.apache.http.impl.client.HttpClientBuilder;

public interface HttpClientCustomizer {

    void customize(final HttpClientBuilder builder);

}
