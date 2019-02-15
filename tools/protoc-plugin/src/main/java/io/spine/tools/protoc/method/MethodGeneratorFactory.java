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
import io.spine.protoc.MethodGenerator;
import io.spine.tools.protoc.GeneratedMethod;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

final class MethodGeneratorFactory {

    /** Prevents instantiation of this utility class. */
    private MethodGeneratorFactory() {
    }

    /**
     * Creates an instance of a {@link MethodGenerator} out of the supplied
     * {@link GeneratedMethod specification}.
     *
     * <p>If specification is invalid or the specified class for some reason could not be
     * instantiated a {@link NoOpMethodGenerator} instance is returned.
     */
    static MethodGenerator forMethodSpec(GeneratedMethod spec) {
        String generatorName = spec.getGeneratorName();
        if (Strings.isNullOrEmpty(generatorName)) {
            return NoOpMethodGenerator.INSTANCE;
        }
        MethodGenerator result = loadMethodGenerator(generatorName);
        return result;
    }

    private static MethodGenerator loadMethodGenerator(String fqn) {
        Optional<Class<MethodGenerator>> generatorClass = loadMethodGeneratorClass(fqn);
        if (!generatorClass.isPresent()) {
            return NoOpMethodGenerator.INSTANCE;
        }
        Logger logger = Logging.get(MethodGeneratorFactory.class);
        try {
            MethodGenerator generator = generatorClass.get()
                                                      .getConstructor()
                                                      .newInstance();
            return generator;
        } catch (InstantiationException e) {
            logger.warn("Unable to instantiate MethodGenerator {}.", fqn, e);
        } catch (IllegalAccessException e) {
            logger.warn("Unable to access MethodGenerator {}.", fqn, e);
        } catch (NoSuchMethodException e) {
            logger.warn("Unable to get constructor for MethodGenerator {}.", fqn, e);
        } catch (InvocationTargetException e) {
            logger.warn("Unable to invoke public constructor for MethodGenerator {}.", fqn, e);
        }
        return NoOpMethodGenerator.INSTANCE;
    }

    private static Optional<Class<MethodGenerator>> loadMethodGeneratorClass(String fqn) {
        Optional<Class<?>> generator = loadGeneratorClass(fqn);
        if (!generator.isPresent()) {
            return Optional.empty();
        }
        Class<?> generatorClass = generator.get();
        if (generatorClass.isAssignableFrom(MethodGenerator.class)) {
            //noinspection unchecked we do already know that the class represents MethodGenerator
            return Optional.of((Class<MethodGenerator>) generatorClass);
        }
        return Optional.empty();
    }

    private static Optional<Class<?>> loadGeneratorClass(String fqn) {
        try {
            Class<?> generator = Class.forName(fqn);
            return Optional.ofNullable(generator);
        } catch (ClassNotFoundException e) {
            Logging.get(MethodGeneratorFactory.class)
                   .warn("Unable to resolve MethodGenerator {}.", fqn, e);
        }
        return Optional.empty();
    }

    /**
     * A no-operation stub implementation of a {@link MethodGenerator} that is used if the
     * method generator is not configured and/or available.
     */
    private static class NoOpMethodGenerator implements MethodGenerator {

        private static final MethodGenerator INSTANCE = new NoOpMethodGenerator();

        @Override
        public List<MethodBody> generate(MessageType messageType) {
            return ImmutableList.of();
        }
    }
}
