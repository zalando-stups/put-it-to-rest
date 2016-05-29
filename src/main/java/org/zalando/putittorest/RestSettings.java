package org.zalando.putittorest;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "rest")
public final class RestSettings {

    private final GlobalOAuth oauth = new GlobalOAuth();
    private final Map<String, Client> clients = new LinkedHashMap<>();

    public GlobalOAuth getOauth() {
        return oauth;
    }

    public Map<String, Client> getClients() {
        return clients;
    }

}
