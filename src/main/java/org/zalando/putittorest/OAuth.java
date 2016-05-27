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

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newLinkedHashSet;

public final class OAuth {

    private boolean enabled = true;
    private final List<String> scopes = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public OAuth merge(final GlobalOAuth that) {
        final OAuth merged = new OAuth();
        merged.setEnabled(isEnabled());
        merged.getScopes().addAll(newLinkedHashSet(concat(getScopes(), that.getScopes())));
        return merged;
    }

}
