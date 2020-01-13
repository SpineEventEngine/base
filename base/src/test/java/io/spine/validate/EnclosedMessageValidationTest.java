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

import com.google.protobuf.Message;
import io.spine.test.validate.EnclosedMessageFieldValue;
import io.spine.test.validate.EnclosedMessageFieldValueWithCustomInvalidMessage;
import io.spine.test.validate.EnclosedMessageFieldValueWithoutAnnotationFieldValueWithCustomInvalidMessage;
import io.spine.test.validate.EnclosedMessageWithRequiredString;
import io.spine.test.validate.EnclosedMessageWithoutAnnotationFieldValue;
import io.spine.test.validate.PatternStringFieldValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.validate.ValidationOfConstraintTest.VALIDATION_SHOULD;
import static io.spine.validate.given.MessageValidatorTestEnv.EMAIL;
import static io.spine.validate.given.MessageValidatorTestEnv.MATCH_REGEXP_MSG;
import static io.spine.validate.given.MessageValidatorTestEnv.OUTER_MSG_FIELD;
import static io.spine.validate.given.MessageValidatorTestEnv.assertFieldPathIs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName(VALIDATION_SHOULD + "validate enclosed messages and")
class EnclosedMessageValidationTest extends ValidationOfConstraintTest {

    @Test
    @DisplayName("find out that enclosed message field is valid")
    void findOutThatEnclosedMessageFieldIsValid() {
        PatternStringFieldValue enclosedMsg =
                PatternStringFieldValue.newBuilder()
                                       .setEmail("valid.email@mail.com")
                                       .build();
        EnclosedMessageFieldValue msg = EnclosedMessageFieldValue.newBuilder()
                                                                 .setOuterMsgField(enclosedMsg)
                                                                 .build();
        assertValid(msg);
    }

    @Test
    @DisplayName("find out enclosed message field is NOT valid")
    void findOutThatEnclosedMessageFieldIsNotValid() {
        PatternStringFieldValue enclosedMsg = PatternStringFieldValue.newBuilder()
                                                                     .setEmail("invalid email")
                                                                     .build();
        EnclosedMessageFieldValue msg = EnclosedMessageFieldValue.newBuilder()
                                                                 .setOuterMsgField(enclosedMsg)
                                                                 .build();
        assertNotValid(msg);
    }

    @Test
    @DisplayName("consider field valid if no valid option is set")
    void considerFieldValidIfNoValidOptionIsSet() {
        PatternStringFieldValue enclosedMsg = PatternStringFieldValue.newBuilder()
                                                                     .setEmail("invalid email")
                                                                     .build();
        EnclosedMessageWithoutAnnotationFieldValue msg =
                EnclosedMessageWithoutAnnotationFieldValue.newBuilder()
                                                          .setOuterMsgField(enclosedMsg)
                                                          .build();
        assertValid(msg);
    }

    @Test
    @DisplayName("consider field valid if it is not set")
    void considerFieldValidIfItIsNotSet() {
        EnclosedMessageWithRequiredString msg = EnclosedMessageWithRequiredString.newBuilder()
                                                                                 .build();
        assertValid(msg);
    }

    @Test
    @DisplayName("provide valid violations if enclosed message field is not valid")
    void provideValidViolationsIfEnclosedMessageFieldIsNotValid() {
        PatternStringFieldValue enclosedMsg = PatternStringFieldValue
                .newBuilder()
                .setEmail("invalid email")
                .build();
        EnclosedMessageFieldValue msg = EnclosedMessageFieldValue
                .newBuilder()
                .setOuterMsgField(enclosedMsg)
                .build();
        validate(msg);

        ConstraintViolation violation = singleViolation();
        assertEquals("The message must have valid properties.", violation.getMsgFormat());
        assertFieldPathIs(violation, OUTER_MSG_FIELD);
        List<ConstraintViolation> innerViolations = violation.getViolationList();
        assertEquals(1, innerViolations.size());

        ConstraintViolation innerViolation = innerViolations.get(0);
        assertEquals(MATCH_REGEXP_MSG, innerViolation.getMsgFormat());
        assertFieldPathIs(innerViolation, OUTER_MSG_FIELD, EMAIL);
        assertTrue(innerViolation.getViolationList()
                                 .isEmpty());
    }

    @Test
    @DisplayName("provide custom invalid field message if specified")
    void provideCustomInvalidFieldMessageIfSpecified() {
        PatternStringFieldValue enclosedMsg = PatternStringFieldValue.newBuilder()
                                                                     .setEmail("invalid email")
                                                                     .build();
        EnclosedMessageFieldValueWithCustomInvalidMessage msg =
                EnclosedMessageFieldValueWithCustomInvalidMessage.newBuilder()
                                                                 .setOuterMsgField(enclosedMsg)
                                                                 .build();
        validate(msg);

        ConstraintViolation violation = singleViolation();
        assertEquals("Custom error", violation.getMsgFormat());
    }

    @Test
    @DisplayName("ignore custom invalid field message if validation is disabled")
    void ignoreCustomInvalidFieldMessageIfValidationIsDisabled() {
        Message msg = EnclosedMessageFieldValueWithoutAnnotationFieldValueWithCustomInvalidMessage
                .getDefaultInstance();
        assertValid(msg);
    }
}
