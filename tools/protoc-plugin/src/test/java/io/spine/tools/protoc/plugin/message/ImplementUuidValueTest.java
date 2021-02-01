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

package io.spine.tools.protoc.plugin.message;

import com.google.protobuf.Descriptors;
import io.spine.tools.protoc.UuidConfig;
import io.spine.tools.protoc.plugin.given.TestInterface;
import io.spine.tools.protoc.message.tests.IdWrongFieldName;
import io.spine.tools.protoc.message.tests.ProjectId;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertNpe;

@DisplayName("`GenerateInterfaces` should")
final class ImplementUuidValueTest {

    @Nested
    @DisplayName("throw `NullPointerException` if")
    class ThrowNpe {

        @Test
        @DisplayName("is created with `null` arguments")
        void isCreatedWithNullArguments() {
            assertNpe(() -> new ImplementUuidValue(null));
        }

        @Test
        @DisplayName("`null` `MessageType` is supplied")
        void nullMessageTypeIsSupplied() {
            UuidConfig config = newTaskConfig("test");
            ImplementUuidValue generateMethods = new ImplementUuidValue(config);
            assertNpe(() -> generateMethods.generateFor(null));
        }
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "  "})
    @DisplayName("throw `IllegalArgumentException` if interface name is")
    void throwIllegalArgumentException(String interfaceName) {
        assertIllegalArgument(() -> newTask(newTaskConfig(interfaceName)));
    }

    @Nested
    @DisplayName("generate empty result if")
    class GenerateEmptyResult {

        @Test
        @DisplayName("message is not UUID")
        void notUuid() {
            assertEmptyResult(TestInterface.class.getName(), IdWrongFieldName.getDescriptor());
        }

        private void assertEmptyResult(String interfaceName, Descriptors.Descriptor descriptor) {
            UuidConfig config = newTaskConfig(interfaceName);
            assertThat(newTask(config).generateFor(new MessageType(descriptor)))
                    .isEmpty();
        }
    }

    @Test
    @DisplayName("implement interface")
    void implementInterface() {
        UuidConfig config = newTaskConfig(TestInterface.class.getName());
        assertThat(newTask(config).generateFor(new MessageType(ProjectId.getDescriptor())))
                .isNotEmpty();
    }

    private static ImplementUuidValue newTask(UuidConfig config) {
        return new ImplementUuidValue(config);
    }

    private static UuidConfig newTaskConfig(String interfaceName) {
        return UuidConfig.newBuilder()
                         .setValue(interfaceName)
                         .build();
    }
}
