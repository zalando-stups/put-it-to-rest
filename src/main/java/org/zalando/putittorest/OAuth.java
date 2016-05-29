package org.zalando.putittorest;

import java.util.ArrayList;
import java.util.List;

public final class OAuth {

    private final List<String> scopes = new ArrayList<>();

    public List<String> getScopes() {
        return scopes;
    }

}
