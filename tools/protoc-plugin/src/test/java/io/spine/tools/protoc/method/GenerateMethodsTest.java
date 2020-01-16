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

import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.FilePattern;
import io.spine.tools.protoc.FilePatterns;
import io.spine.tools.protoc.given.TestMethodFactory;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GenerateMethods should")
final class GenerateMethodsTest {

    @DisplayName("throw NullPointerException if")
    @Nested
    class ThrowNpe {

        @DisplayName("is created with `null` arguments")
        @Test
        void isCreatedWithNullArguments() {
            assertThrows(NullPointerException.class, () ->
                    new GenerateMethods(null, ConfigByPattern.getDefaultInstance()));
            assertThrows(NullPointerException.class, () ->
                    new GenerateMethods(testMethodFactories(), null)
            );
        }

        @DisplayName("`null` MessageType is supplied")
        @Test
        void nullMessageTypeIsSupplied() {
            ConfigByPattern config = newTaskConfig("test")
                    .setPattern(FilePatterns.filePrefix("non-default"))
                    .build();
            GenerateMethods generateMethods = new GenerateMethods(testMethodFactories(), config);
            assertThrows(NullPointerException.class, () -> generateMethods.generateFor(null));
        }
    }

    @DisplayName("reject empty `FilePattern`")
    @Test
    void rejectEmptyFilePattern() {
        assertThrows(IllegalArgumentException.class, () ->
                newTask(newTaskConfig("not-empty-name").build()));
    }

    @DisplayName("throw IllegalArgumentException if factory name is ")
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    void throwIllegalArgumentException(String factoryName) {
        ConfigByPattern config = newTaskConfig(factoryName)
                .setPattern(FilePatterns.filePrefix("non-default"))
                .build();
        assertThrows(IllegalArgumentException.class, () ->
                new GenerateMethods(testMethodFactories(), config));
    }

    @DisplayName("generate empty result if")
    @Nested
    class GenerateEmptyResult {

        @DisplayName("message does not match pattern")
        @Test
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

    @DisplayName("generate new methods")
    @Test
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
        return new GenerateMethods(testMethodFactories(), config);
    }

    private static MethodFactories testMethodFactories() {
        return new MethodFactories(Classpath.getDefaultInstance());
    }

    private static MessageType testType() {
        return new MessageType(EnhancedWithPatternBasedCodeGeneration.getDescriptor());
    }
}
