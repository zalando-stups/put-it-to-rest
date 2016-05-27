package org.zalando.putittorest;

/*
 * ⁣​
 * Put it to REST!
 * ⁣⁣
 * Copyright (C) 2016 Zalando SE
 * ⁣⁣
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ​⁣
 */

import org.springframework.beans.factory.FactoryBean;
import org.zalando.stups.tokens.AccessTokens;
import org.zalando.stups.tokens.AccessTokensBuilder;
import org.zalando.stups.tokens.Tokens;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.zalando.putittorest.Timeouts.toMillis;

class AccessTokensFactoryBean implements FactoryBean<AccessTokens> {

    private AccessTokensBuilder builder;

    public void setSettings(final RestSettings settings) {
        final GlobalOAuth oauth = settings.getOauth();
        final Timeouts timeouts = oauth.getTimeouts();
        final URI accessTokenUrl = getAccessTokenUrl(oauth);

        this.builder = Tokens.createAccessTokensWithUri(accessTokenUrl)
                .schedulingPeriod(oauth.getSchedulingPeriod())
                .connectTimeout(toMillis(timeouts.getConnect()))
                .socketTimeout(toMillis(timeouts.getRead()));

        settings.getClients().forEach((id, client) -> {
            builder.manageToken(id)
                    .addScopes(Optional.ofNullable(client.getOauth())
                            .map(OAuth::getScopes)
                            .orElse(emptyList()))
                    .done();
        });
    }

    private URI getAccessTokenUrl(GlobalOAuth oauth) {
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
