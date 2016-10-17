package org.zalando.putittorest;

import lombok.Data;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

@Data
public final class RestSettings {

    private final Defaults defaults = new Defaults();
    private final GlobalOAuth oauth = new GlobalOAuth();
    private final Map<String, Client> clients = new LinkedHashMap<>();

    @Data
    public static final class Defaults {
        private TimeSpan connectionTimeout = TimeSpan.of(5, SECONDS);
        private TimeSpan socketTimeout = TimeSpan.of(5, SECONDS);
        private TimeSpan connectionTimeToLive = TimeSpan.of(30, SECONDS);
        private int maxConnectionsPerRoute = 2;
        private int maxConnectionsTotal = 20;
    }

    @Data
    public static final class GlobalOAuth {
        private URI accessTokenUrl;
        private TimeSpan schedulingPeriod = TimeSpan.of(5, SECONDS);
        private TimeSpan connectionTimeout = TimeSpan.of(1, SECONDS);
        private TimeSpan socketTimeout = TimeSpan.of(2, SECONDS);
        private TimeSpan connectionTimeToLive;
    }

    @Data
    public static final class Client {
        private String baseUrl;
        private TimeSpan connectionTimeout;
        private TimeSpan socketTimeout;
        private TimeSpan connectionTimeToLive;
        private int maxConnectionsPerRoute;
        private int maxConnectionsTotal;
        private OAuth oauth;
        private boolean compressRequest = false;
        private Keystore keystore;
    }

    @Data
    public static final class OAuth {
        private final List<String> scopes = new ArrayList<>();
    }

    @Data
    public static final class Keystore {
        private String path;
        private String password;
    }

}
