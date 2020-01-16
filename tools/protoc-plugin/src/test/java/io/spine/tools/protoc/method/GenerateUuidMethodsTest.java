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
import io.spine.tools.protoc.UuidConfig;
import io.spine.tools.protoc.given.TestMethodFactory;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GenerateUuidMethods should")
final class GenerateUuidMethodsTest {

    @DisplayName("throw NullPointerException if")
    @Nested
    class ThrowNpe {

        @DisplayName("is created with `null` arguments")
        @Test
        void isCreatedWithNullArguments() {
            assertThrows(NullPointerException.class, () ->
                    new GenerateUuidMethods(null, UuidConfig.getDefaultInstance()));
            assertThrows(NullPointerException.class, () ->
                    new GenerateUuidMethods(testMethodFactories(), null)
            );
        }

        @DisplayName("`null` MessageType is supplied")
        @Test
        void nullMessageTypeIsSupplied() {
            UuidConfig config = newTaskConfig("test");
            GenerateUuidMethods task = new GenerateUuidMethods(testMethodFactories(), config);
            assertThrows(NullPointerException.class, () -> task.generateFor(null));
        }
    }

    @DisplayName("throw IllegalArgumentException if factory name is ")
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    void throwIllegalArgumentException(String factoryName) {
        UuidConfig config = newTaskConfig(factoryName);
        assertThrows(IllegalArgumentException.class, () ->
                new GenerateUuidMethods(testMethodFactories(), config));
    }

    @DisplayName("generate empty result if")
    @Nested
    class GenerateEmptyResult {

        @DisplayName("message is not UUID value")
        @Test
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

    @DisplayName("generate new methods")
    @Test
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
        return new GenerateUuidMethods(testMethodFactories(), config);
    }

    private static MethodFactories testMethodFactories() {
        return new MethodFactories(Classpath.getDefaultInstance());
    }

    private static MessageType testType() {
        return new MessageType(TestUuidValue.getDescriptor());
    }
}
