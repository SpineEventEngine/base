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

import com.google.common.testing.NullPointerTester;
import io.spine.tools.java.protoc.ConfigByPattern;
import io.spine.tools.java.protoc.FilePatterns;
import io.spine.tools.java.protoc.NestedClassFactory;
import io.spine.tools.mc.java.protoc.given.TestNestedClassFactory;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.mc.java.protoc.ExternalClassLoader;
import io.spine.tools.mc.java.protoc.message.GenerateNestedClasses;
import io.spine.tools.mc.java.protoc.method.given.TestClassLoader;
import io.spine.tools.mc.java.protoc.nested.TaskView;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.java.protoc.FilePatterns.filePrefix;

@DisplayName("`GenerateNestedClasses` should")
class GenerateNestedClassesTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicInstanceMethods(newTask());
    }

    @Test
    @DisplayName("throw `IAE` if `FilePattern` is not set")
    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
        // Method called to throw exception.
    void rejectEmptyFilePattern() {
        assertIllegalArgument(() -> newTask(newTaskConfig().build()));
    }

    @Test
    @DisplayName("reject empty factory name")
    void rejectEmptyFactoryName() {
        String emptyName = "";
        ConfigByPattern config = newTaskConfig(emptyName)
                .setPattern(filePrefix("non-default"))
                .build();
        assertIllegalArgument(() -> new GenerateNestedClasses(testClassLoader(), config));
    }

    @Test
    @DisplayName("reject effectively emtpy factory name")
    void rejectEffectivelyEmptyFactoryName() {
        String effectivelyEmptyName = "   ";
        ConfigByPattern config = newTaskConfig(effectivelyEmptyName)
                .setPattern(filePrefix("non-default"))
                .build();
        assertIllegalArgument(() -> new GenerateNestedClasses(testClassLoader(), config));
    }

    @Nested
    @DisplayName("generate empty result if")
    class GenerateEmptyResult {

        @Test
        @DisplayName("message does not match pattern")
        void messageDoesNotMatchPattern() {
            ConfigByPattern config = newTaskConfig()
                    .setPattern(FilePatterns.filePrefix("non-matching"))
                    .build();
            Collection<CompilerOutput> output = newTask(config).generateFor(testType());
            assertThat(output).isEmpty();
        }
    }

    @Test
    @DisplayName("generate nested classes if the message matches the pattern")
    void generateNewMethods() {
        ConfigByPattern config = newTaskConfig()
                .setPattern(FilePatterns.fileSuffix("test_fields.proto"))
                .build();
        Collection<CompilerOutput> output = newTask(config).generateFor(testType());
        assertThat(output).isNotEmpty();
    }

    static GenerateNestedClasses newTask() {
        return newTask(newTaskConfig()
                               .setPattern(FilePatterns.fileSuffix("test_columns.proto"))
                               .build());
    }

    static GenerateNestedClasses newTask(ConfigByPattern config) {
        return new GenerateNestedClasses(testClassLoader(), config);
    }

    private static ConfigByPattern.Builder newTaskConfig() {
        return newTaskConfig(TestNestedClassFactory.class.getName());
    }

    private static ConfigByPattern.Builder newTaskConfig(String factoryName) {
        return ConfigByPattern.newBuilder()
                              .setValue(factoryName);
    }

    static ExternalClassLoader<NestedClassFactory> testClassLoader() {
        return TestClassLoader.instance();
    }

    private static MessageType testType() {
        return new MessageType(TaskView.getDescriptor());
    }
}
