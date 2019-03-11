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

package io.spine.tools.protoc.iface;

import com.google.protobuf.Descriptors;
import io.spine.tools.protoc.UuidImplementInterface;
import io.spine.tools.protoc.given.TestInterface;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GenerateInterfaces should")
final class GenerateUuidInterfacesTest {

    @DisplayName("throw NullPointerException if")
    @Nested
    class ThrowNpe {

        @DisplayName("is created with `null` arguments")
        @Test
        void isCreatedWithNullArguments() {
            assertThrows(NullPointerException.class, () ->
                    new GenerateUuidInterfaces(null));
        }

        @DisplayName("`null` MessageType is supplied")
        @Test
        void nullMessageTypeIsSupplied() {
            UuidImplementInterface config = newTaskConfig("test");
            GenerateUuidInterfaces generateMethods = new GenerateUuidInterfaces(config);
            assertThrows(NullPointerException.class, () -> generateMethods.generateFor(null));
        }
    }

    @DisplayName("generate empty result if")
    @Nested
    class GenerateEmptyResult {

        @DisplayName("interface name is empty")
        @Test
        void factoryNameIsEmpty() {
            assertEmptyResult("", ProjectCreated.getDescriptor());
        }

        @DisplayName("message is not UUID")
        @Test
        void notUuid() {
            assertEmptyResult(TestInterface.class.getName(), IdWrongFieldName.getDescriptor());
        }

        private void assertEmptyResult(String interfaceName, Descriptors.Descriptor descriptor) {
            UuidImplementInterface config = newTaskConfig(interfaceName);
            assertThat(newTask(config).generateFor(new MessageType(descriptor)))
                    .isEmpty();
        }
    }

    @DisplayName("implement interface")
    @Test
    void implementInterface() {
        UuidImplementInterface config = newTaskConfig(TestInterface.class.getName());
        assertThat(newTask(config).generateFor(new MessageType(ProjectId.getDescriptor())))
                .isNotEmpty();
    }

    private static GenerateUuidInterfaces newTask(UuidImplementInterface config) {
        return new GenerateUuidInterfaces(config);
    }

    private static UuidImplementInterface newTaskConfig(String interfaceName) {
        return UuidImplementInterface.newBuilder()
                                     .setInterfaceName(interfaceName)
                                     .build();
    }
}
