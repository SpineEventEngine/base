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

package io.spine.code.generate.java;

import io.spine.test.code.generate.uuid.UuidMessage;
import io.spine.tools.protoc.method.GeneratedMethod;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UuidMethodFactory should")
final class UuidMethodFactoryTest {

    private final UuidMethodFactory factory = new UuidMethodFactory();

    @DisplayName("not allow null values")
    @Test
    void notAllowNulls() {
        assertThrows(NullPointerException.class, () -> factory.createFor(null));
    }

    @DisplayName("create new")
    @Nested
    final class CreateNew {

        @DisplayName("`generate` method")
        @Test
        void generateMethod() {
            MessageType uuidType = new MessageType(UuidMessage.getDescriptor());
            List<GeneratedMethod> methods = factory.createFor(uuidType);
            GeneratedMethod generate = methods.get(0);
            assertThat(generate.value())
                    .isEqualTo("/**\n" +
                                       " * Creates a new instance with a random UUID value.\n" +
                                       " * @see java.util.UUID#randomUUID\n" +
                                       " */\n" +
                                       "public static final io.spine.test.code.generate.uuid.UuidMessage generate() {\n" +
                                       "  return newBuilder().setUuid(java.util.UUID.randomUUID().toString()).build();\n" +
                                       "}\n");
        }

        @DisplayName("`of` method")
        @Test
        void ofMethod() {
            MessageType uuidType = new MessageType(UuidMessage.getDescriptor());
            List<GeneratedMethod> methods = factory.createFor(uuidType);
            GeneratedMethod of = methods.get(1);
            assertThat(of.value())
                    .isEqualTo("/**\n" +
                                       " * Creates a new instance from the passed value.\n" +
                                       " * @throws java.lang.IllegalArgumentException if the passed value is not a valid UUID string\n" +
                                       " */\n" +
                                       "public static final io.spine.test.code.generate.uuid.UuidMessage of(java.lang.String uuid) {\n" +
                                       "  io.spine.util.Preconditions2.checkNotEmptyOrBlank(uuid);\n" +
                                       "  try {\n" +
                                       "    java.util.UUID.fromString(uuid);\n" +
                                       "  } catch(java.lang.NumberFormatException e) {\n" +
                                       "    throw io.spine.util.Exceptions.newIllegalArgumentException(e, \"Invalid UUID string: %s\", uuid);\n" +
                                       "  }\n" +
                                       "  return newBuilder().setUuid(uuid).build();\n" +
                                       "}\n");
        }
    }
}
