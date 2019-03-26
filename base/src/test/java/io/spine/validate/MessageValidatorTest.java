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

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.validate.given.MessageValidatorTestEnv.assertFieldPathIs;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class MessageValidatorTest {

    static final String MESSAGE_VALIDATOR_SHOULD = "MessageValidator should ";

    private List<ConstraintViolation> violations;

    void validate(Message msg) {
        MessageValidator validator = MessageValidator.newInstance(msg);
        violations = validator.validate();
    }

    ConstraintViolation firstViolation() {
        return violations.get(0);
    }

    ConstraintViolation singleViolation() {
        assertThat(violations).hasSize(1);
        return violations.get(0);
    }

    void assertValid(Message msg) {
        validate(msg);
        assertIsValid(true);
    }

    void assertNotValid(Message msg) {
        validate(msg);
        assertIsValid(false);
    }

    void assertNotValid(Message msg, boolean checkFieldPath) {
        validate(msg);
        assertIsValid(false, checkFieldPath);
    }

    void assertIsValid(boolean isValid) {
        assertIsValid(isValid, true);
    }

    void assertIsValid(boolean isValid, boolean checkFieldPath) {
        if (isValid) {
            assertTrue(violations.isEmpty(), () -> violations.toString());
        } else {
            assertViolations(violations, checkFieldPath);
        }
    }

    private static void assertViolations(List<ConstraintViolation> violations,
                                         boolean checkFieldPath) {
        assertFalse(violations.isEmpty());
        for (ConstraintViolation violation : violations) {
            assertHasCorrectFormat(violation);
            if (checkFieldPath) {
                assertHasFieldPath(violation);
            }
        }
    }

    private static void assertHasCorrectFormat(ConstraintViolation violation) {
        String format = violation.getMsgFormat();
        assertTrue(!format.isEmpty());
        boolean noParams = violation.getParamList()
                                    .isEmpty();
        if (format.contains("%s")) {
            assertFalse(noParams);
        } else {
            assertTrue(noParams);
        }
    }

    private static void assertHasFieldPath(ConstraintViolation violation) {
        assertFalse(violation.getFieldPath()
                             .getFieldNameList()
                             .isEmpty());
    }

    void assertSingleViolation(Message message,
                               String expectedErrMsg,
                               String invalidFieldName) {
        assertNotValid(message);
        assertEquals(1, violations.size());
        assertSingleViolation(expectedErrMsg, invalidFieldName);
    }

    /** Checks that a message is not valid and has a single violation. */
    void assertSingleViolation(String expectedErrMsg, String invalidFieldName) {
        ConstraintViolation violation = firstViolation();
        String actualErrorMessage = format(violation.getMsgFormat(), violation.getParamList()
                                                                              .toArray());
        assertEquals(expectedErrMsg, actualErrorMessage);
        assertFieldPathIs(violation, invalidFieldName);
        assertTrue(violation.getViolationList()
                            .isEmpty());
    }
}
