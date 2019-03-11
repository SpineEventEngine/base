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

import com.google.common.collect.ImmutableList;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.MethodFactoryConfiguration;
import io.spine.tools.protoc.UuidGenerateMethod;
import io.spine.tools.protoc.given.TestMethodFactory;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GenerateUuidMethods should")
public class GenerateUuidMethodsTest {

    @DisplayName("throw NullPointerException if")
    @Nested
    class ThrowNpe {

        @DisplayName("is created with `null` arguments")
        @Test
        void isCreatedWithNullArguments() {
            assertThrows(NullPointerException.class, () ->
                    new GenerateUuidMethods(null, UuidGenerateMethod.getDefaultInstance()));
            assertThrows(NullPointerException.class, () ->
                    new GenerateUuidMethods(testMethodFactories(), null)
            );
        }

        @DisplayName("`null` MessageType is supplied")
        @Test
        void nullMessageTypeIsSupplied() {
            UuidGenerateMethod config = newTaskConfig("test");
            GenerateUuidMethods task = new GenerateUuidMethods(testMethodFactories(), config);
            assertThrows(NullPointerException.class, () -> task.generateFor(null));
        }
    }

    @DisplayName("generate empty result if")
    @Nested
    class GenerateEmptyResult {

        @DisplayName("factory name is empty")
        @Test
        void factoryNameIsEmpty() {
            assertEmptyResult("");
        }

        @DisplayName("message is not UUID value")
        @Test
        void notUuid() {
            assertEmptyResult(TestMethodFactory.class.getName());
        }

        private void assertEmptyResult(String factoryName) {
            UuidGenerateMethod config = newTaskConfig(factoryName);
            ImmutableList<CompilerOutput> result = newTask(config)
                    .generateFor(new MessageType(NonEnhancedMessage.getDescriptor()));
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("generate new methods")
    @Test
    void generateNewMethods() {
        UuidGenerateMethod config = newTaskConfig(TestMethodFactory.class.getName());
        assertThat(newTask(config).generateFor(testType()))
                .isNotEmpty();
    }

    private static UuidGenerateMethod newTaskConfig(String factoryName) {
        return UuidGenerateMethod.newBuilder()
                                 .setFactoryName(factoryName)
                                 .build();
    }

    private static GenerateUuidMethods newTask(UuidGenerateMethod config) {
        return new GenerateUuidMethods(testMethodFactories(), config);
    }

    private static MethodFactories testMethodFactories() {
        return new MethodFactories(MethodFactoryConfiguration.getDefaultInstance());
    }

    private static MessageType testType() {
        return new MessageType(TestUuidValue.getDescriptor());
    }
}
