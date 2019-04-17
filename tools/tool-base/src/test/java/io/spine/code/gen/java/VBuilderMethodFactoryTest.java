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

package io.spine.code.gen.java;

import com.google.protobuf.Descriptors;
import com.google.protobuf.StringValue;
import io.spine.option.EntityOption;
import io.spine.test.code.generate.vbuilder.VBMFTExampleMessage;
import io.spine.test.code.generate.vbuilder.rejections.Rejections;
import io.spine.tools.protoc.method.GeneratedMethod;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("VBuilderMethodFactory should")
final class VBuilderMethodFactoryTest {

    private final VBuilderMethodFactory factory = new VBuilderMethodFactory();

    @DisplayName("not allow null values")
    @Test
    void notAllowNulls() {
        assertThrows(NullPointerException.class, () -> factory.createFor(null));
    }

    @DisplayName("generate empty result for")
    @Nested
    final class GenerateEmptyResult {

        @DisplayName("rejection")
        @Test
        void rejection() {
            assertEmptyResult(Rejections.VBuilderNotGenerated.getDescriptor());
        }

        @DisplayName("google messages")
        @Test
        void google() {
            assertEmptyResult(StringValue.getDescriptor());
        }

        @DisplayName("protobuf option messages")
        @Test
        void options() {
            assertEmptyResult(EntityOption.getDescriptor());
        }

        private void assertEmptyResult(Descriptors.Descriptor descriptor) {
            MessageType type = new MessageType(descriptor);
            assertThat(factory.createFor(type))
                    .isEmpty();
        }
    }

    @DisplayName("generate")
    @Nested
    final class Generate {

        @DisplayName("vBuilder static method")
        @Test
        void vBuilderMethod() {
            MessageType type = new MessageType(VBMFTExampleMessage.getDescriptor());
            List<GeneratedMethod> newMethods = factory.createFor(type);
            assertEquals(2, newMethods.size());
            assertThat(newMethods.get(0)
                                 .value())
                    .isEqualTo("/**\n" +
                                       " * Creates a new instance of a {@link io.spine.test.code.generate.vbuilder.VBMFTExampleMessageVBuilder}.\n" +
                                       " */\n" +
                                       "public static final io.spine.test.code.generate.vbuilder.VBMFTExampleMessageVBuilder vBuilder() {\n" +
                                       "  return io.spine.test.code.generate.vbuilder.VBMFTExampleMessageVBuilder.newBuilder();\n" +
                                       "}\n");
        }

        @DisplayName("toVBuilder instance method")
        @Test
        void toVBuilderMethod() {
            MessageType type = new MessageType(VBMFTExampleMessage.getDescriptor());
            List<GeneratedMethod> newMethods = factory.createFor(type);
            assertEquals(2, newMethods.size());
            assertThat(newMethods.get(1)
                                 .value())
                    .isEqualTo("/**\n" +
                                       " * Creates a new instance of a {@link io.spine.test.code.generate.vbuilder.VBMFTExampleMessageVBuilder} with the current state.\n" +
                                       " */\n" +
                                       "public final io.spine.test.code.generate.vbuilder.VBMFTExampleMessageVBuilder toVBuilder() {\n" +
                                       "  io.spine.test.code.generate.vbuilder.VBMFTExampleMessageVBuilder result = io.spine.test.code.generate.vbuilder.VBMFTExampleMessageVBuilder.newBuilder();\n" +
                                       "  result.setOriginalState(this);\n" +
                                       "  return result;\n" +
                                       "}\n");
        }
    }
}
