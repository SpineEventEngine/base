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

import io.spine.tools.java.protoc.Classpath;
import io.spine.tools.java.protoc.ConfigByPattern;
import io.spine.tools.java.protoc.FilePattern;
import io.spine.tools.java.protoc.FilePatterns;
import io.spine.tools.java.protoc.MethodFactory;
import io.spine.tools.mc.java.protoc.ExternalClassLoader;
import io.spine.tools.mc.java.protoc.given.TestMethodFactory;
import io.spine.tools.mc.java.protoc.method.EnhancedWithPatternBasedCodeGeneration;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertNpe;
import static io.spine.tools.java.protoc.FilePatterns.filePrefix;

@DisplayName("`GenerateMethods` should")
final class GenerateMethodsTest {

    @Nested
    @DisplayName("throw `NullPointerException` if")
    class ThrowNpe {

        @Test
        @DisplayName("is created with `null` arguments")
        void isCreatedWithNullArguments() {
            assertNpe(() -> new GenerateMethods(null, ConfigByPattern.getDefaultInstance()));
            assertNpe(() -> new GenerateMethods(testClassLoader(), null));
        }

        @Test
        @DisplayName("`null` `MessageType` is supplied")
        void nullMessageTypeIsSupplied() {
            ConfigByPattern config = newTaskConfig("test")
                    .setPattern(filePrefix("non-default"))
                    .build();
            GenerateMethods generateMethods = new GenerateMethods(testClassLoader(), config);
            assertNpe(() -> generateMethods.generateFor(null));
        }
    }

    @Test
    @DisplayName("reject empty `FilePattern`")
    void rejectEmptyFilePattern() {
        assertIllegalArgument(() -> newTask(newTaskConfig("not-empty-name").build()));
    }

    @DisplayName("throw `IllegalArgumentException` if factory name is ")
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    void throwIllegalArgumentException(String factoryName) {
        ConfigByPattern config = newTaskConfig(factoryName)
                .setPattern(filePrefix("non-default"))
                .build();
        assertIllegalArgument(() -> new GenerateMethods(testClassLoader(), config));
    }

    @Nested
    @DisplayName("generate empty result if")
    class GenerateEmptyResult {

        @Test
        @DisplayName("message does not match pattern")
        void messageDoesNotMatchPattern() {
            assertEmptyResult(TestMethodFactory.class.getName(), FilePatterns.fileRegex("wrong"));
        }

        private void assertEmptyResult(String factoryName, FilePattern filePattern) {
            ConfigByPattern config = newTaskConfig(factoryName)
                    .setPattern(filePattern)
                    .build();
            assertThat(newTask(config).generateFor(testType()))
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("generate new methods")
    void generateNewMethods() {
        ConfigByPattern config = newTaskConfig(TestMethodFactory.class.getName())
                .setPattern(FilePatterns.fileSuffix("test_patterns.proto"))
                .build();
        assertThat(newTask(config).generateFor(testType()))
                .isNotEmpty();
    }

    private static ConfigByPattern.Builder newTaskConfig(String factoryName) {
        return ConfigByPattern.newBuilder()
                              .setValue(factoryName);
    }

    private static GenerateMethods newTask(ConfigByPattern config) {
        return new GenerateMethods(testClassLoader(), config);
    }

    private static ExternalClassLoader<MethodFactory> testClassLoader() {
        return new ExternalClassLoader<>(Classpath.getDefaultInstance(), MethodFactory.class);
    }

    private static MessageType testType() {
        return new MessageType(EnhancedWithPatternBasedCodeGeneration.getDescriptor());
    }
}
