package org.zalando.putittorest;

import java.net.URI;

public final class GlobalOAuth {

    private URI accessTokenUrl;
    private int schedulingPeriod = 5;
    private final Timeouts timeouts = new Timeouts(1, 2);

    public URI getAccessTokenUrl() {
        return accessTokenUrl;
    }

    public void setAccessTokenUrl(final URI accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public int getSchedulingPeriod() {
        return schedulingPeriod;
    }

    public void setSchedulingPeriod(final int schedulingPeriod) {
        this.schedulingPeriod = schedulingPeriod;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

}
