package org.zalando.putittorest;

import org.springframework.beans.factory.FactoryBean;
import org.zalando.stups.tokens.AccessTokens;
import org.zalando.stups.tokens.AccessTokensBuilder;
import org.zalando.stups.tokens.Tokens;

import javax.annotation.Nullable;
import java.net.URI;

import static org.zalando.putittorest.Timeouts.toMillis;

class AccessTokensFactoryBean implements FactoryBean<AccessTokens> {

    private AccessTokensBuilder builder;

    public void setSettings(final RestSettings settings) {
        final GlobalOAuth oAuth = settings.getOauth();
        final Timeouts timeouts = oAuth.getTimeouts();
        final URI accessTokenUrl = getAccessTokenUrl(oAuth);

        this.builder = Tokens.createAccessTokensWithUri(accessTokenUrl)
                .schedulingPeriod(oAuth.getSchedulingPeriod())
                .connectTimeout(toMillis(timeouts.getConnect()))
                .socketTimeout(toMillis(timeouts.getRead()));
        // TODO custom HttpProvider with interceptors

        settings.getClients().forEach((id, client) -> {
            @Nullable final OAuth clientOAuth = client.getOauth();

            if (clientOAuth == null) {
                return;
            }

            builder.manageToken(id)
                    .addScopes(clientOAuth.getScopes())
                    .done();
        });
    }

    private URI getAccessTokenUrl(final GlobalOAuth oauth) {
        @Nullable final URI accessTokenUrl = oauth.getAccessTokenUrl();

        if (accessTokenUrl == null) {
            return URI.create(System.getenv("ACCESS_TOKEN_URL"));
        }

        return accessTokenUrl;
    }


    @Override
    public AccessTokens getObject() {
        return builder.start();
    }

    @Override
    public Class<?> getObjectType() {
        return AccessTokens.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
