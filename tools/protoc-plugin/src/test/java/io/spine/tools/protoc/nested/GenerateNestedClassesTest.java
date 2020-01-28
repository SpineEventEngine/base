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

package io.spine.tools.protoc.nested;

import com.google.common.testing.NullPointerTester;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.ExternalClassLoader;
import io.spine.tools.protoc.FilePatterns;
import io.spine.tools.protoc.given.TestNestedClassFactory;
import io.spine.tools.protoc.nested.given.TestClassLoader;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`GenerateNestedClasses` should")
final class GenerateNestedClassesTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicConstructors(GenerateNestedClasses.class);
        new NullPointerTester()
                .testAllPublicInstanceMethods(newTask());
    }

    @Test
    @DisplayName("throw `IAE` if `FilePattern` is not set")
    void rejectEmptyFilePattern() {
        assertThrows(IllegalArgumentException.class, () ->
                newTask(newTaskConfig().build()));
    }

    @Test
    @DisplayName("reject empty factory name")
    void rejectEmptyFactoryName() {
        String emptyName = "";
        ConfigByPattern config = newTaskConfig(emptyName)
                .setPattern(FilePatterns.filePrefix("non-default"))
                .build();
        assertThrows(IllegalArgumentException.class, () ->
                new GenerateNestedClasses(testClassLoader(), config));
    }

    @Test
    @DisplayName("reject effectively emtpy factory name")
    void rejectEffectivelyEmptyFactoryName() {
        String effectivelyEmptyName = "   ";
        ConfigByPattern config = newTaskConfig(effectivelyEmptyName)
                .setPattern(FilePatterns.filePrefix("non-default"))
                .build();
        assertThrows(IllegalArgumentException.class, () ->
                new GenerateNestedClasses(testClassLoader(), config));
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
            assertThat(newTask(config).generateFor(testType()))
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("generate nested classes if the message matches the pattern")
    void generateNewMethods() {
        ConfigByPattern config = newTaskConfig()
                .setPattern(FilePatterns.fileSuffix("test_fields.proto"))
                .build();
        assertThat(newTask(config).generateFor(testType()))
                .isNotEmpty();
    }

    private static GenerateNestedClasses newTask() {
        return newTask(newTaskConfig()
                               .setPattern(FilePatterns.fileSuffix("test_columns.proto"))
                               .build());
    }

    private static GenerateNestedClasses newTask(ConfigByPattern config) {
        return new GenerateNestedClasses(testClassLoader(), config);
    }

    private static ConfigByPattern.Builder newTaskConfig() {
        return newTaskConfig(TestNestedClassFactory.class.getName());
    }

    private static ConfigByPattern.Builder newTaskConfig(String factoryName) {
        return ConfigByPattern.newBuilder()
                              .setValue(factoryName);
    }

    private static ExternalClassLoader<NestedClassFactory> testClassLoader() {
        return TestClassLoader.instance();
    }

    private static MessageType testType() {
        return new MessageType(TaskView.getDescriptor());
    }
}
