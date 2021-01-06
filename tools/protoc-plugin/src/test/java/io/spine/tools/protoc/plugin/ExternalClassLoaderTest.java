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

package io.spine.tools.protoc.plugin;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import io.spine.testing.logging.MuteLogging;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.plugin.method.GeneratedMethod;
import io.spine.tools.protoc.plugin.method.MethodFactory;
import io.spine.tools.protoc.plugin.ClassInstantiationException;
import io.spine.tools.protoc.plugin.ExternalClassLoader;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`ExternalClassLoader` should")
final class ExternalClassLoaderTest {

    private ExternalClassLoader<MethodFactory> classLoader;

    @BeforeEach
    void setUp() {
        Classpath instance = Classpath.getDefaultInstance();
        classLoader = new ExternalClassLoader<>(instance, MethodFactory.class);
    }

    @DisplayName("throw `IllegalArgumentException` if the class name is")
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    void throwIllegalArgumentException(String factoryName) {
        assertIllegalArgument(() -> classLoader.newInstance(factoryName));
    }

    @Nested
    @MuteLogging
    @DisplayName("throw `ClassInstantiationException`")
    @SuppressWarnings("NonExceptionNameEndsWithException")
    class ThrowInstantiationException {

        @Nested
        @DisplayName("if implementation")
        class IfImplementation {

            @Test
            @DisplayName("does not have a public constructor")
            void withoutPublicConstructor() {
                assertCannotCreateInstance(WithoutPublicConstructor.class);
            }

            @Test
            @DisplayName("has private constructor")
            void withPrivateConstructor() {
                assertCannotCreateInstance(WithPrivateConstructor.class);
            }

            @Test
            @DisplayName("is abstract")
            void implementationIsAbstract() {
                assertCannotCreateInstance(WithAbstractImplementation.class);
            }

            @Test
            @DisplayName("is not found or not available")
            @SuppressWarnings("CheckReturnValue") // The method called to throw an exception.
            void classIsNotFound() {
                assertCannotInstantiate(
                        () -> classLoader.newInstance("com.example.NonExistingClass")
                );
            }
        }

        @Test
        @DisplayName("if exception is thrown during instantiation")
        void exceptionThrownDuringInstantiation() {
            assertCannotCreateInstance(WithExceptionDuringInstantiation.class);
        }

        @Test
        @DisplayName("if supplied class does not implement the loaded class")
        void doesNotImplementLoadedClass() {
            assertCannotCreateInstance(NotMethodFactory.class);
        }

        void assertCannotCreateInstance(Class<?> cls) {
            assertCannotInstantiate(() -> newInstanceFor(cls));
        }

        void assertCannotInstantiate(Executable e) {
            assertThrows(ClassInstantiationException.class, e);
        }
    }

    @Test
    @DisplayName("return a class instance by its fully-qualified name")
    void returnClassInstanceByFqn() {
        assertThat(newInstanceFor(StubMethodFactory.class))
                .isInstanceOf(StubMethodFactory.class);
    }

    @CanIgnoreReturnValue
    private MethodFactory newInstanceFor(Class<?> clazz) {
        return classLoader.newInstance(clazz.getName());
    }

    @Immutable
    private static class EmptyMethodFactory implements MethodFactory {

        @Override
        public ImmutableList<GeneratedMethod> createFor(MessageType messageType) {
            return ImmutableList.of();
        }
    }

    @Immutable
    public static final class StubMethodFactory extends EmptyMethodFactory {

        public StubMethodFactory() {
        }
    }

    @Immutable
    @SuppressWarnings("EmptyClass") // for test reasons
    private static final class WithoutPublicConstructor extends EmptyMethodFactory {
    }

    @SuppressWarnings("WeakerAccess")
    @Immutable
    public static final class WithPrivateConstructor extends EmptyMethodFactory {

        private WithPrivateConstructor() {
        }
    }

    @Immutable
    static final class WithExceptionDuringInstantiation extends EmptyMethodFactory {

        WithExceptionDuringInstantiation() {
            throw new RuntimeException("Test exception during instantiation");
        }
    }

    @Immutable
    @SuppressWarnings("AbstractClassNeverImplemented") // For test reasons.
    abstract static class WithAbstractImplementation extends EmptyMethodFactory {

        WithAbstractImplementation() {
        }
    }

    static final class NotMethodFactory {

        NotMethodFactory() {
        }
    }
}
