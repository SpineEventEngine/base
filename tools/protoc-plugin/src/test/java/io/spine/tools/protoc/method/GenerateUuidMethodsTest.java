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

import com.google.common.collect.ImmutableList;
import io.spine.tools.protoc.Classpath;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.ExternalClassLoader;
import io.spine.tools.protoc.UuidConfig;
import io.spine.tools.protoc.given.TestMethodFactory;
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
            assertNpe(() -> new GenerateUuidMethods(null, UuidConfig.getDefaultInstance()));
            assertNpe(() -> new GenerateUuidMethods(testClassLoader(), null));
        }

        @Test
        @DisplayName("`null` `MessageType` is supplied")
        void nullMessageTypeIsSupplied() {
            UuidConfig config = newTaskConfig("test");
            GenerateUuidMethods task = new GenerateUuidMethods(testClassLoader(), config);
            assertNpe(() -> task.generateFor(null));
        }
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    @DisplayName("throw `IllegalArgumentException` if factory name is ")
    void throwIllegalArgumentException(String factoryName) {
        UuidConfig config = newTaskConfig(factoryName);
        assertIllegalArgument(() -> new GenerateUuidMethods(testClassLoader(), config));
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
            UuidConfig config = newTaskConfig(factoryName);
            ImmutableList<CompilerOutput> result = newTask(config)
                    .generateFor(new MessageType(NonEnhancedMessage.getDescriptor()));
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("generate new methods")
    void generateNewMethods() {
        UuidConfig config = newTaskConfig(TestMethodFactory.class.getName());
        assertThat(newTask(config).generateFor(testType()))
                .isNotEmpty();
    }

    private static UuidConfig newTaskConfig(String factoryName) {
        return UuidConfig.newBuilder()
                                 .setValue(factoryName)
                                 .build();
    }

    private static GenerateUuidMethods newTask(UuidConfig config) {
        return new GenerateUuidMethods(testClassLoader(), config);
    }

    private static ExternalClassLoader<MethodFactory> testClassLoader() {
        return new ExternalClassLoader<>(Classpath.getDefaultInstance(), MethodFactory.class);
    }

    private static MessageType testType() {
        return new MessageType(TestUuidValue.getDescriptor());
    }
}
