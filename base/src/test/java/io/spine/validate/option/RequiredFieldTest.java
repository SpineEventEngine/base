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

package io.spine.validate.option;

import io.spine.test.validate.requiredfield.ComplexRequiredFields;
import io.spine.test.validate.requiredfield.EveryFieldOptional;
import io.spine.test.validate.requiredfield.EveryFieldRequired;
import io.spine.test.validate.requiredfield.OneofFieldAndOtherFieldRequired;
import io.spine.test.validate.requiredfield.OneofRequired;
import io.spine.validate.ValidationOfConstraintTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.validate.ValidationOfConstraintTest.VALIDATION_SHOULD;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName(VALIDATION_SHOULD + "analyze (required_field) message option and consider message")
final class RequiredFieldTest extends ValidationOfConstraintTest {

    @DisplayName("valid if")
    @Nested
    final class Valid {

        @DisplayName("all required fields are set")
        @Test
        void allRequiredFieldsAreSet() {
            EveryFieldRequired message = EveryFieldRequired
                    .newBuilder()
                    .setFirst("first field set")
                    .setSecond("second field set")
                    .setThird("third field set")
                    .build();
            assertValid(message);
        }

        @DisplayName("oneof field")
        @Nested
        final class OneofField {

            @DisplayName("'first' is set")
            @Test
            void first() {
                OneofRequired withFirstField = OneofRequired
                        .newBuilder()
                        .setFirst("first field set")
                        .build();
                assertValid(withFirstField);
            }

            @DisplayName("'second' is set")
            @Test
            void second() {
                OneofRequired withSecondField = OneofRequired
                        .newBuilder()
                        .setSecond("second field set")
                        .build();
                assertValid(withSecondField);
            }
        }

        @Disabled("See https://github.com/SpineEventEngine/base/issues/381")
        @DisplayName("oneof and other field are set")
        @Test
        void oneofAndOtherFieldsAreSet() {
            OneofFieldAndOtherFieldRequired message = OneofFieldAndOtherFieldRequired
                    .newBuilder()
                    .setSecond("second field set")
                    .setThird("third field set")
                    .build();
            assertValid(message);
        }

        @DisplayName("all fields are optional")
        @Test
        void optionalFieldsAreNotSet() {
            assertValid(EveryFieldOptional.getDefaultInstance());
            EveryFieldOptional message = EveryFieldOptional
                    .newBuilder()
                    .setFirst("first field set")
                    .setThird("third field set")
                    .build();
            assertValid(message);
        }

        @Disabled("See https://github.com/SpineEventEngine/base/issues/381")
        @DisplayName("a message qualifies for complex required field pattern")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.RequiredFieldTest#validComplexMessages")
        void qualifiesForComplexRequiredFieldPattern(ComplexRequiredFields message) {
            assertValid(message);
        }
    }

    @SuppressWarnings("unused") // used via `@MethodSource` value
    private static Stream<ComplexRequiredFields> validComplexMessages() {
        ComplexRequiredFields message = ComplexRequiredFields
                .newBuilder()
                .addFirst("first field set")
                .setFourth("fourth field set")
                .setFifth(ComplexRequiredFields.FifthField
                                  .newBuilder()
                                  .setValue("fifthFieldValue"))
                .build();
        ComplexRequiredFields alternativeMessage = ComplexRequiredFields
                .newBuilder()
                .putSecond("key", "second field set")
                .setThird("fourth field set")
                .setFifth(ComplexRequiredFields.FifthField
                                  .newBuilder()
                                  .setValue("fifthFieldValue"))
                .build();
        return Stream.of(message, alternativeMessage);
    }

    @DisplayName("invalid if")
    @Nested
    final class Invalid {

        @DisplayName("a required field is not set")
        @Test
        void requiredFieldIsNotSet() {
            assertNotValid(EveryFieldRequired.getDefaultInstance(), false);
            EveryFieldRequired onlyOneRequiredSet = EveryFieldRequired
                    .newBuilder()
                    .setFirst("only one set")
                    .build();
            assertNotValid(onlyOneRequiredSet, false);

            EveryFieldRequired twoRequiredSet = EveryFieldRequired
                    .newBuilder()
                    .setFirst("first set")
                    .setSecond("second set")
                    .build();
            assertNotValid(twoRequiredSet, false);
        }

        @DisplayName("a required oneof is not set")
        @Test
        void oneofNotSet() {
            assertNotValid(OneofRequired.getDefaultInstance(), false);
            OneofRequired withDefaultValue = OneofRequired
                    .newBuilder()
                    .setFirst("")
                    .build();
            assertNotValid(withDefaultValue, false);
        }

        @DisplayName("oneof or other field is not set")
        @Test
        void oneofOrOtherNotSet() {
            IllegalStateException exception =
                    assertThrows(IllegalStateException.class,
                                 () -> validate(OneofFieldAndOtherFieldRequired.getDefaultInstance()));
            assertThat(exception)
                    .hasMessageThat()
                    .contains("(");
        }

        @Disabled("See https://github.com/SpineEventEngine/base/issues/381")
        @DisplayName("a message does not qualifies for a complext required field pattern")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.RequiredFieldTest#invalidComplexMessages")
        void notQualifiesForComplexPattern(ComplexRequiredFields message) {
            assertNotValid(message, false);
        }
    }

    @SuppressWarnings("unused") // invoked via `@MethodSource`.
    private static Stream<ComplexRequiredFields> invalidComplexMessages() {
        ComplexRequiredFields.FifthField.Builder fifthFieldValue =
                ComplexRequiredFields.FifthField.newBuilder()
                                                .setValue("fifthFieldValue");
        ComplexRequiredFields withoutListOrMap = ComplexRequiredFields
                .newBuilder()
                .setFourth("fourth field set")
                .setFifth(fifthFieldValue)
                .build();

        ComplexRequiredFields withoutOneof = ComplexRequiredFields
                .newBuilder()
                .putSecond("key", "second field set")
                .setFifth(fifthFieldValue)
                .build();

        ComplexRequiredFields withoutMessage = ComplexRequiredFields
                .newBuilder()
                .putSecond("key", "second field set")
                .setThird("fourth field set")
                .build();
        return Stream.of(ComplexRequiredFields.getDefaultInstance(),
                         withoutListOrMap,
                         withoutOneof,
                         withoutMessage);
    }
}
