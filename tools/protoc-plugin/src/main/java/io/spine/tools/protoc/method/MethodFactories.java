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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.spine.code.proto.MessageType;
import io.spine.logging.Logging;
import io.spine.protoc.MethodBody;
import io.spine.protoc.MethodFactory;
import io.spine.tools.protoc.GeneratedMethod;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

final class MethodFactories {

    /** Prevents instantiation of this utility class. */
    private MethodFactories() {
    }

    /**
     * Creates an instance of a {@link MethodFactory} out of the supplied
     * {@link GeneratedMethod specification}.
     *
     * <p>If specification is invalid or the specified class for some reason could not be
     * instantiated a {@link NoOpMethodFactory} instance is returned.
     */
    static MethodFactory newFactoryFor(GeneratedMethod spec) {
        String generatorName = spec.getGeneratorName();
        if (Strings.isNullOrEmpty(generatorName)) {
            return NoOpMethodFactory.INSTANCE;
        }
        MethodFactory result = from(generatorName);
        return result;
    }

    /**
     * Instantiates a new {@link MethodFactory} from the specified fully-qualified class name.
     */
    private static MethodFactory from(String fqn) {
        Optional<Class<MethodFactory>> factoryClass = methodFactoryClass(fqn);
        if (!factoryClass.isPresent()) {
            return NoOpMethodFactory.INSTANCE;
        }
        Logger logger = Logging.get(MethodFactories.class);
        try {
            MethodFactory generator = factoryClass.get()
                                                  .getConstructor()
                                                  .newInstance();
            return generator;
        } catch (InstantiationException e) {
            logger.warn("Unable to instantiate MethodFactory {}.", fqn, e);
        } catch (IllegalAccessException e) {
            logger.warn("Unable to access MethodFactory {}.", fqn, e);
        } catch (NoSuchMethodException e) {
            logger.warn("Unable to get constructor for MethodFactory {}.", fqn, e);
        } catch (InvocationTargetException e) {
            logger.warn("Unable to invoke public constructor for MethodFactory {}.", fqn, e);
        }
        return NoOpMethodFactory.INSTANCE;
    }

    private static Optional<Class<MethodFactory>> methodFactoryClass(String fqn) {
        Optional<Class<?>> generator = factoryClass(fqn);
        if (!generator.isPresent()) {
            return Optional.empty();
        }
        Class<?> generatorClass = generator.get();
        if (generatorClass.isAssignableFrom(MethodFactory.class)) {
            //noinspection unchecked we do already know that the class represents MethodFactory
            return Optional.of((Class<MethodFactory>) generatorClass);
        }
        return Optional.empty();
    }

    private static Optional<Class<?>> factoryClass(String fqn) {
        try {
            Class<?> generator = Class.forName(fqn);
            return Optional.ofNullable(generator);
        } catch (ClassNotFoundException e) {
            Logging.get(MethodFactories.class)
                   .warn("Unable to resolve MethodFactory {}.", fqn, e);
        }
        return Optional.empty();
    }

    /**
     * A no-operation stub implementation of a {@link MethodFactory} that is used if the
     * method generator is not configured and/or available.
     */
    private static class NoOpMethodFactory implements MethodFactory {

        private static final MethodFactory INSTANCE = new NoOpMethodFactory();

        @Override
        public List<MethodBody> newMethodsFor(MessageType ignored) {
            return ImmutableList.of();
        }
    }
}
