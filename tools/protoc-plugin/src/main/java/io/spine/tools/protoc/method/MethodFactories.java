/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.logging.Logging;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.GeneratedMethod;
import io.spine.tools.protoc.MethodFactoryConfiguration;
import io.spine.type.MessageType;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * An utility class for instantiating {@link MethodFactory} for a particular
 * {@link GeneratedMethod} specification.
 */
final class MethodFactories implements Logging {

    private final ClassLoader externalClassLoader;

    MethodFactories(MethodFactoryConfiguration configuration) {
        this.externalClassLoader = externalClassLoader(configuration);
    }

    /**
     * Creates an instance of a {@link MethodFactory} out of the supplied
     * {@link GeneratedMethod specification}.
     *
     * <p>If specification is invalid or the specified class for some reason could not be
     * instantiated a {@link NoOpMethodFactory} instance is returned.
     */
    MethodFactory newFactoryFor(String factoryName) {
        checkNotNull(factoryName);
        if (factoryName.trim()
                       .isEmpty()) {
            return NoOpMethodFactory.INSTANCE;
        }
        MethodFactory result = from(factoryName);
        return result;
    }

    /**
     * Instantiates a new {@link MethodFactory} from the specified fully-qualified class name.
     */
    private MethodFactory from(String fqn) {
        Optional<Class<MethodFactory>> factoryClass = methodFactoryClass(fqn);
        if (!factoryClass.isPresent()) {
            return NoOpMethodFactory.INSTANCE;
        }
        try {
            MethodFactory factory = factoryClass.get()
                                                .getConstructor()
                                                .newInstance();
            return factory;
        } catch (InstantiationException e) {
            _warn("Unable to instantiate MethodFactory {}.", fqn, e);
        } catch (IllegalAccessException e) {
            _warn("Unable to access MethodFactory {}.", fqn, e);
        } catch (NoSuchMethodException e) {
            _warn("Unable to get constructor for MethodFactory {}.", fqn, e);
        } catch (InvocationTargetException e) {
            _warn("Unable to invoke public constructor for MethodFactory {}.", fqn, e);
        }
        return NoOpMethodFactory.INSTANCE;
    }

    @SuppressWarnings("unchecked") //we do already know that the class represents MethodFactory
    private Optional<Class<MethodFactory>> methodFactoryClass(String fqn) {
        Optional<Class<?>> factory = factoryClass(fqn);
        if (!factory.isPresent()) {
            return Optional.empty();
        }
        Class<?> factoryClass = factory.get();
        if (MethodFactory.class.isAssignableFrom(factoryClass)) {
            return Optional.of((Class<MethodFactory>) factoryClass);
        }
        _warn("Class {} does not implement io.spine.tools.protoc.method.MethodFactory.", fqn);
        return Optional.empty();
    }

    private Optional<Class<?>> factoryClass(String fqn) {
        try {
            Class<?> factory = externalClassLoader.loadClass(fqn);
            return Optional.ofNullable(factory);
        } catch (ClassNotFoundException e) {
            _warn("Unable to resolve MethodFactory {}.", fqn, e);
        }
        return Optional.empty();
    }

    private static ClassLoader externalClassLoader(MethodFactoryConfiguration configuration) {
        ClassLoader currentClassLoader = Thread.currentThread()
                                               .getContextClassLoader();
        URL[] classPathUrls = classPathUrls(configuration);
        URLClassLoader loader = URLClassLoader.newInstance(classPathUrls, currentClassLoader);
        return loader;
    }

    private static URL[] classPathUrls(MethodFactoryConfiguration configuration) {
        Classpath classpath = configuration.getClasspath();
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

    /**
     * A no-operation stub implementation of a {@link MethodFactory} that is used if the
     * method factory is not configured and/or available.
     */
    @VisibleForTesting
    @Immutable
    static class NoOpMethodFactory implements MethodFactory {

        @VisibleForTesting
        static final MethodFactory INSTANCE = new NoOpMethodFactory();

        @Override
        public List<MethodBody> newMethodsFor(MessageType ignored) {
            return ImmutableList.of();
        }
    }
}
