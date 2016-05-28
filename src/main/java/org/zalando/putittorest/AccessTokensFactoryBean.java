package org.zalando.putittorest;

/*
 * ⁣​
 * Put it to REST!
 * ⁣⁣
 * Copyright (C) 2016 Zalando SE
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
