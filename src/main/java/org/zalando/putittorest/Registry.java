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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

final class Registry {

    private final BeanDefinitionRegistry registry;

    Registry(final BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public boolean isRegistered(final String name) {
        return registry.isBeanNameInUse(name);
    }

    public boolean isNotRegistered(final String name) {
        return !isRegistered(name);
    }

    public <T> String register(final Class<T> type, final Supplier<BeanDefinitionBuilder> factory) {
        final String name = UPPER_CAMEL.to(LOWER_CAMEL, type.getSimpleName());

        if (registry.isBeanNameInUse(name)) {
            return name;
        }

        registry.registerBeanDefinition(name, factory.get().getBeanDefinition());

        return name;
    }

    public <T> String register(final String id, final Class<T> type,
            final Supplier<BeanDefinitionBuilder> factory) {

        final String name = generateBeanName(id, type);

        if (registry.isBeanNameInUse(name)) {
            return name;
        }

        final AbstractBeanDefinition definition = factory.get().getBeanDefinition();

        final AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(Qualifier.class);
        qualifier.setAttribute("value", id);
        definition.addQualifier(qualifier);

        registry.registerBeanDefinition(name, definition);

        return name;
    }

    public static  <T> String generateBeanName(final String id, final Class<T> type) {
        return LOWER_HYPHEN.to(LOWER_CAMEL, id) + type.getSimpleName();
    }

    public static BeanReference ref(final String beanName) {
        return new RuntimeBeanReference(beanName);
    }

    public static List<Object> list(final Object... elements) {
        final ManagedList<Object> list = new ManagedList<>();
        Collections.addAll(list, elements);
        return list;
    }

}
