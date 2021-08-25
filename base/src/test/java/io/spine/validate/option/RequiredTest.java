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

package io.spine.validate.option;

import com.google.protobuf.StringValue;
import io.spine.test.validate.CustomMessageWithNoRequiredOption;
import io.spine.test.validate.Planet;
import io.spine.test.validate.RepeatedRequiredMsgFieldValue;
import io.spine.test.validate.RequiredBooleanFieldValue;
import io.spine.test.validate.RequiredByteStringFieldValue;
import io.spine.test.validate.RequiredEnumFieldValue;
import io.spine.test.validate.RequiredMsgFieldValue;
import io.spine.test.validate.RequiredStringFieldValue;
import io.spine.testing.logging.mute.MuteLogging;
import io.spine.validate.ValidationOfConstraintTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static io.spine.validate.ValidationOfConstraintTest.VALIDATION_SHOULD;
import static io.spine.validate.given.MessageValidatorTestEnv.VALUE;
import static io.spine.validate.given.MessageValidatorTestEnv.newByteString;
import static io.spine.validate.given.MessageValidatorTestEnv.newStringValue;

@DisplayName(VALIDATION_SHOULD + "analyze (required) option and")
class RequiredTest extends ValidationOfConstraintTest {

    @Test
    @DisplayName("find out that required Message field is set")
    void findOutThatRequiredMessageFieldIsSet() {
        RequiredMsgFieldValue validMsg = RequiredMsgFieldValue
                .newBuilder()
                .setValue(newStringValue())
                .build();
        assertValid(validMsg);
    }

    @Test
    @DisplayName("find out that required message field is NOT set")
    void findOutThatRequiredMessageFieldIsNotSet() {
        RequiredMsgFieldValue invalidMsg = RequiredMsgFieldValue.getDefaultInstance();
        assertNotValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that required String field is set")
    void findOutThatRequiredStringFieldIsSet() {
        RequiredStringFieldValue validMsg = RequiredStringFieldValue.newBuilder()
                                                                    .setValue(newUuid())
                                                                    .build();
        assertValid(validMsg);
    }

    @Test
    @DisplayName("find out that required String field is NOT set")
    void findOutThatRequiredStringFieldIsNotSet() {
        RequiredStringFieldValue invalidMsg = RequiredStringFieldValue.getDefaultInstance();
        assertNotValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that required ByteString field is set")
    void findOutThatRequiredByteStringFieldIsSet() {
        RequiredByteStringFieldValue validMsg =
                RequiredByteStringFieldValue.newBuilder()
                                            .setValue(newByteString())
                                            .build();
        assertValid(validMsg);
    }

    @Test
    @DisplayName("find out that required ByteString field is NOT set")
    void findOutThatRequiredByteStringFieldIsNotSet() {
        RequiredByteStringFieldValue invalidMsg = RequiredByteStringFieldValue.getDefaultInstance();
        assertNotValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that required Enum field is set")
    void findOutThatRequiredEnumFieldIsNotSet() {
        RequiredEnumFieldValue invalidMsg = RequiredEnumFieldValue.getDefaultInstance();
        assertNotValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that required Enum field is NOT set")
    void findOutThatRequiredEnumFieldIsSet() {
        RequiredEnumFieldValue invalidMsg = RequiredEnumFieldValue.newBuilder()
                                                                  .setValue(Planet.EARTH)
                                                                  .build();
        assertValid(invalidMsg);
    }

    @MuteLogging
    @Test
    @DisplayName("find out that required NOT set Boolean field passes validation")
    void findOutThatRequiredNotSetBooleanFieldPassValidation() {
        RequiredBooleanFieldValue msg = RequiredBooleanFieldValue.getDefaultInstance();
        assertValid(msg);
    }

    @Test
    @DisplayName("find out that repeated required field has valid values")
    void findOutThatRepeatedRequiredFieldHasValidValues() {
        RepeatedRequiredMsgFieldValue invalidMsg =
                RepeatedRequiredMsgFieldValue.newBuilder()
                                             .addValue(newStringValue())
                                             .addValue(newStringValue())
                                             .build();
        assertValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that repeated required field has not values")
    void findOutThatRepeatedRequiredFieldHasNoValues() {
        RepeatedRequiredMsgFieldValue msg = RepeatedRequiredMsgFieldValue.getDefaultInstance();
        assertNotValid(msg);
    }

    @Test
    @DisplayName("ignore repeated required field with an empty value")
    void ignoreRepeatedRequiredFieldWithEmptyValue() {
        RepeatedRequiredMsgFieldValue msg = RepeatedRequiredMsgFieldValue
                .newBuilder()
                .addValue(newStringValue()) // valid value
                .addValue(StringValue.getDefaultInstance()) // empty value
                .build();
        assertValid(msg);
    }

    @Test
    @DisplayName("consider field is valid if no required option set")
    void considerFieldIsValidIfNoRequiredOptionSet() {
        StringValue msg = StringValue.getDefaultInstance();
        assertValid(msg);
    }

    @Test
    @DisplayName("provide one valid violation if required field is NOT set")
    void provideOneValidViolationIfRequiredFieldIsNOTSet() {
        RequiredStringFieldValue invalidMsg = RequiredStringFieldValue.getDefaultInstance();
        assertSingleViolation(invalidMsg, VALUE);
    }

    @Test
    @DisplayName("ignore IfMissingOption if field is not marked required")
    void ignoreIfMissingOptionIfFieldNotMarkedRequired() {
        CustomMessageWithNoRequiredOption invalidMsg =
                CustomMessageWithNoRequiredOption.getDefaultInstance();
        assertValid(invalidMsg);
    }
}
