/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.protoc;

import io.spine.logging.Logging;
import io.spine.tools.protoc.Classpath;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A utility for instantiating a particular class from its {@linkplain FullyQualifiedName FQN}
 * using the specified classpath.
 *
 * @param <T>
 *         the loaded class
 */
public final class ExternalClassLoader<T> implements Logging {

    private final ClassLoader classLoader;
    private final Class<T> loadedClass;

    public ExternalClassLoader(Classpath classpath, Class<T> loadedClass) {
        this.classLoader = classLoader(classpath);
        this.loadedClass = loadedClass;
    }

    public T newInstance(@FullyQualifiedName String className) {
        checkNotEmptyOrBlank(className);
        T result = from(className);
        return result;
    }

    /**
     * Instantiates the class defined by the specified fully-qualified name.
     */
    private T from(String fqn) {
        Class<T> clazz = loadClass(fqn);
        try {
            T instance = clazz.getConstructor()
                              .newInstance();
            return instance;
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            _error().withCause(e)
                    .log("Unable to instantiate the class `%s`.", fqn);
            throw new ClassInstantiationException(fqn, e);
        }
    }

    @SuppressWarnings("unchecked") // The class is already checked to be assignable during the cast.
    private Class<T> loadClass(String fqn) {
        Class<?> factory = classByFqn(fqn);
        if (loadedClass.isAssignableFrom(factory)) {
            return (Class<T>) factory;
        }
        _error().log("Class `%s` does not implement `%s`.", fqn, loadedClass.getName());
        throw new ClassInstantiationException(fqn);
    }

    private Class<?> classByFqn(String fqn) {
        try {
            Class<?> factory = classLoader.loadClass(fqn);
            return factory;
        } catch (ClassNotFoundException e) {
            _error().log("Unable to resolve class `%s`.", fqn);
            throw new ClassInstantiationException(fqn, e);
        }
    }

    private static ClassLoader classLoader(Classpath factoryClasspath) {
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
                .map(ExternalClassLoader::toUrl)
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
