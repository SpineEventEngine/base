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

package io.spine.tools.mc.java.protoc.method;

import com.google.common.collect.ImmutableList;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.mc.java.protoc.ExternalClassLoader;
import io.spine.tools.mc.java.protoc.given.TestMethodFactory;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.JavaClassName;
import io.spine.tools.protoc.MethodFactory;
import io.spine.tools.protoc.MethodFactoryName;
import io.spine.tools.protoc.Uuids;
import io.spine.tools.protoc.plugin.method.NonEnhancedMessage;
import io.spine.tools.protoc.plugin.method.TestUuidValue;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertNpe;

@DisplayName("`GenerateUuidMethods` should")
final class GenerateUuidMethodsTest {

    @Nested
    @DisplayName("throw `NullPointerException` if")
    class ThrowNpe {

        @Test
        @DisplayName("is created with `null` arguments")
        void isCreatedWithNullArguments() {
            assertNpe(() -> new GenerateUuidMethods(null, MethodFactoryName.getDefaultInstance()));
            assertNpe(() -> new GenerateUuidMethods(testClassLoader(), null));
        }

        @Test
        @DisplayName("`null` `MessageType` is supplied")
        void nullMessageTypeIsSupplied() {
            Uuids config = newTaskConfig("test");
            GenerateUuidMethods task = newTask(config);
            assertNpe(() -> task.generateFor(null));
        }
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    @DisplayName("throw `IllegalArgumentException` if factory name is ")
    void throwIllegalArgumentException(String factoryName) {
        Uuids config = newTaskConfig(factoryName);
        assertIllegalArgument(() -> newTask(config));
    }

    @Nested
    @DisplayName("generate empty result if")
    class GenerateEmptyResult {

        @Test
        @DisplayName("message is not UUID value")
        void notUuid() {
            assertEmptyResult(TestMethodFactory.class.getName());
        }

        private void assertEmptyResult(String factoryName) {
            Uuids config = newTaskConfig(factoryName);
            ImmutableList<CompilerOutput> result = newTask(config)
                    .generateFor(new MessageType(NonEnhancedMessage.getDescriptor()));
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("generate new methods")
    void generateNewMethods() {
        Uuids config = newTaskConfig(TestMethodFactory.class.getName());
        assertThat(newTask(config).generateFor(testType()))
                .isNotEmpty();
    }

    private static Uuids newTaskConfig(String factoryName) {
        JavaClassName factoryClass = JavaClassName.newBuilder()
                .setCanonical(factoryName)
                .build();
        MethodFactoryName name = MethodFactoryName.newBuilder()
                .setClassName(factoryClass)
                .build();
        return Uuids.newBuilder()
                .addMethodFactory(name)
                .build();
    }

    private static GenerateUuidMethods newTask(Uuids config) {
        return new GenerateUuidMethods(testClassLoader(), config.getMethodFactory(0));
    }

    private static ExternalClassLoader<MethodFactory> testClassLoader() {
        return new ExternalClassLoader<>(Classpath.getDefaultInstance(), MethodFactory.class);
    }

    private static MessageType testType() {
        return new MessageType(TestUuidValue.getDescriptor());
    }
}
