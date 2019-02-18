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

import io.spine.code.proto.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("MessageOptions should")
final class MessageOptionsTest {

    @DisplayName("assert that")
    @Nested
    class AssertThat {

        @DisplayName("option is available")
        @ParameterizedTest(name = "\"{0}\" in \"{1}\"")
        @MethodSource("io.spine.tools.protoc.method.MessageOptionsTest#positiveTests")
        void messagesWithOptionsAreSupported(String messageOption, MessageType type) {
            Assertions.assertTrue(MessageOptions.hasOption(messageOption, type));
        }

        @DisplayName("option is not available")
        @ParameterizedTest(name = "\"{0}\" in \"{1}\"")
        @MethodSource("io.spine.tools.protoc.method.MessageOptionsTest#negativeTests")
        void messagesWithoutOptionsAreNotSupported(String messageOption, MessageType type) {
            Assertions.assertFalse(MessageOptions.hasOption(messageOption, type));
        }
    }

    private static Stream<Arguments> positiveTests() {
        return Stream.of(
                Arguments.of(
                        "enrichment_for", MessageType.of(WithEnrichmentFor.getDescriptor())
                ),
                Arguments.of(
                        "SPI_type", MessageType.of(WithSpiType.getDescriptor())
                ),
                Arguments.of(
                        "experimental_type", MessageType.of(WithExperimentalType.getDescriptor())
                ),
                Arguments.of(
                        "beta_type", MessageType.of(WithBetaType.getDescriptor())
                ),
                Arguments.of(
                        "validation_of", MessageType.of(WithValidationOf.getDescriptor())
                )
        );
    }

    private static Stream<Arguments> negativeTests() {
        return Stream.of(
                Arguments.of(
                        "validation_of", MessageType.of(EnrichedMessage.getDescriptor())
                ),
                Arguments.of(
                        "beta_type", MessageType.of(EnrichedMessage.getDescriptor())
                ),
                Arguments.of(
                        "experimental_type", MessageType.of(EnrichedMessage.getDescriptor())
                ),
                Arguments.of(
                        "SPI_type", MessageType.of(EnrichedMessage.getDescriptor())
                ),
                Arguments.of(
                        "enrichment_for", MessageType.of(EnrichedMessage.getDescriptor())
                )
        );
    }
}
