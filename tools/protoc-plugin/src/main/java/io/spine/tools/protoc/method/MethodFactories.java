/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.tools.protoc.method;

import io.spine.logging.Logging;
import io.spine.tools.protoc.Classpath;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * An utility class for instantiating {@link MethodFactory}.
 */
final class MethodFactories implements Logging {

    private final ClassLoader externalClassLoader;

    MethodFactories(Classpath factoryClasspath) {
        this.externalClassLoader = externalClassLoader(factoryClasspath);
    }

    /**
     * Instantiates a {@link MethodFactory} with the specified {@code factoryName}.
     */
    MethodFactory newFactory(String factoryName) {
        checkNotEmptyOrBlank(factoryName);
        MethodFactory result = from(factoryName);
        return result;
    }

    /**
     * Instantiates a new {@link MethodFactory} from the specified fully-qualified class name.
     */
    private MethodFactory from(String fqn) {
        Class<MethodFactory> factoryClass = methodFactoryClass(fqn);
        try {
            MethodFactory factory = factoryClass.getConstructor()
                                                .newInstance();
            return factory;
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            _error().withCause(e)
                    .log("Unable to create method factory `%s`.", fqn);
            throw new MethodFactoryInstantiationException(fqn, e);
        }
    }

    @SuppressWarnings("unchecked") // factory is already assignable from MethodFactory during cast
    private Class<MethodFactory> methodFactoryClass(String fqn) {
        Class<?> factory = factoryClass(fqn);
        if (MethodFactory.class.isAssignableFrom(factory)) {
            return (Class<MethodFactory>) factory;
        }
        _error().log("Class `%s` does not implement `%s`.", fqn, MethodFactory.class.getName());
        throw new MethodFactoryInstantiationException(fqn);
    }

    private Class<?> factoryClass(String fqn) {
        try {
            Class<?> factory = externalClassLoader.loadClass(fqn);
            return factory;
        } catch (ClassNotFoundException e) {
            _error().log("Unable to resolve `MethodFactory` `%s`.", fqn);
            throw new MethodFactoryInstantiationException(fqn, e);
        }
    }

    private static ClassLoader externalClassLoader(Classpath factoryClasspath) {
        ClassLoader currentClassLoader = Thread.currentThread()
                                               .getContextClassLoader();
        URL[] classPathUrls = classPathUrls(factoryClasspath);
        URLClassLoader loader = URLClassLoader.newInstance(classPathUrls, currentClassLoader);
        return loader;
    }

    private static URL[] classPathUrls(Classpath classpath) {
        return classpath
                .getJarList()
                .stream()
                .map(File::new)
                .map(File::toURI)
                .map(MethodFactories::toUrl)
                .toArray(URL[]::new);
    }

    private static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw newIllegalArgumentException("Could not retrieve classpath dependency '%s'.",
                                              uri, e);
        }
    }
}
