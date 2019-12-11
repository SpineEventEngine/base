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

package io.spine.validate;

import io.spine.test.validate.oneof.OneofAndOtherAreRequired;
import io.spine.test.validate.oneof.OneofWithOptionalFields;
import io.spine.test.validate.oneof.OneofWithRequiredFields;
import io.spine.test.validate.oneof.OneofWithValidation;
import io.spine.test.validate.oneof.RequiredOneofWithValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.MessageValidatorTest.MESSAGE_VALIDATOR_SHOULD;

@DisplayName(MESSAGE_VALIDATOR_SHOULD + "consider oneof")
final class OneofTest extends MessageValidatorTest {

    @DisplayName("valid if")
    @Nested
    final class Valid {

        @Test
        @DisplayName("a required field is set to a non-default value")
        void requiredFieldIsNotDefault() {
            OneofWithRequiredFields requiredIsSet = OneofWithRequiredFields
                    .newBuilder()
                    .setFirst(newUuid())
                    .build();
            assertValid(requiredIsSet);
        }

        @Test
        @DisplayName("all required fields are set")
        void allRequiredFieldsAreNotDefault() {
            OneofAndOtherAreRequired requiredAreSet = OneofAndOtherAreRequired
                    .newBuilder()
                    .setSecond(newUuid())
                    .setThird(newUuid())
                    .build();
            assertValid(requiredAreSet);
        }

        @Test
        @DisplayName("an optional field is set to the default value")
        void optionalIsDefault() {
            OneofWithOptionalFields optionalIsDefault = OneofWithOptionalFields
                    .newBuilder()
                    .setFirst("")
                    .build();
            assertValid(optionalIsDefault);
            assertValid(OneofWithOptionalFields.getDefaultInstance());
        }

        @Test
        @DisplayName("an optional field is properly validated")
        void optionalIsValid() {
            OneofWithValidation validFieldSet = OneofWithValidation
                    .newBuilder()
                    .setWithValidation("valid")
                    .build();
            assertValid(validFieldSet);
        }

        @Test
        @DisplayName("an optional validated field is is default")
        void optionalFieldWithValidationIsDefault() {
            assertValid(OneofWithValidation.getDefaultInstance());
        }

        @Test
        @DisplayName("an optional field without validation is set")
        void optionalFieldWithoutValidationSet() {
            OneofWithValidation fieldWithoutValidationSet = OneofWithValidation
                    .newBuilder()
                    .setNoValidation("does not require validation")
                    .build();
            assertValid(fieldWithoutValidationSet);
        }

        @Test
        @DisplayName("a required field without validation is set")
        void requiredNonValidatedFieldSet() {
            RequiredOneofWithValidation requiredWithoutValidationSet = RequiredOneofWithValidation
                    .newBuilder()
                    .setRawValue("o_0")
                    .build();
            assertValid(requiredWithoutValidationSet);
        }

        @Test
        @DisplayName("a required field with validation is set")
        void requiredValidatedFieldSet() {
            RequiredOneofWithValidation requiredWithValidationSet = RequiredOneofWithValidation
                    .newBuilder()
                    .setValidValue("aaa1111")
                    .build();
            assertValid(requiredWithValidationSet);
        }
    }

    @DisplayName("invalid if")
    @Nested
    final class Invalid {

        @Test
        @DisplayName("a required field is set to the default value")
        void requiredFieldIsDefault() {
            OneofWithRequiredFields requiredIsDefault = OneofWithRequiredFields
                    .newBuilder()
                    .setFirst("")
                    .build();
            assertNotValid(requiredIsDefault, false);
        }

        @Test
        @DisplayName("a field within oneof is not valid")
        void fieldIsNotValid() {
            OneofWithValidation validFieldSet = OneofWithValidation
                    .newBuilder()
                    .setWithValidation("   ")
                    .build();
            assertNotValid(validFieldSet);
        }

        @Test
        @DisplayName("a required field is not set")
        void requiredFieldNotSet() {
            assertNotValid(OneofWithRequiredFields.getDefaultInstance(), false);
        }

        @Test
        @DisplayName("a required field is not valid")
        void requiredFieldIsNotValid() {
            RequiredOneofWithValidation requiredWithValidationSet = RequiredOneofWithValidation
                    .newBuilder()
                    .setValidValue("###")
                    .build();
            assertNotValid(requiredWithValidationSet);
        }
    }
}
