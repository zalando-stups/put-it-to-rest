package org.zalando.putittorest;

/*
 * ⁣​
 * Put it to REST!
 * ⁣⁣
 * Copyright (C) 2015 - 2016 Zalando SE
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class GlobalOAuth {

    private String clientId;
    private URI accessTokenUrl;
    private int schedulingPeriod = 5;
    private final Timeouts timeouts = new Timeouts(1, 2);
    private final List<String> scopes = new ArrayList<>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public URI getAccessTokenUrl() {
        return accessTokenUrl;
    }

    public void setAccessTokenUrl(URI accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public int getSchedulingPeriod() {
        return schedulingPeriod;
    }

    public void setSchedulingPeriod(int schedulingPeriod) {
        this.schedulingPeriod = schedulingPeriod;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public List<String> getScopes() {
        return scopes;
    }

}
