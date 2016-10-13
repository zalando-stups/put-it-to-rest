package org.zalando.putittorest;

import javax.annotation.Nullable;

public final class Client {

    private String baseUrl;
    private OAuth oauth;
    private final Timeouts timeouts = new Timeouts(5, 5);
    private boolean gzipRequestEntity = false;

    @Nullable
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Nullable
    public OAuth getOauth() {
        return oauth;
    }

    public void setOauth(final OAuth oauth) {
        this.oauth = oauth;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public boolean isGzipRequestEntity() {
        return gzipRequestEntity;
    }

    public void setGzipRequestEntity(final boolean gzipRequestEntity) {
        this.gzipRequestEntity = gzipRequestEntity;
    }
}
