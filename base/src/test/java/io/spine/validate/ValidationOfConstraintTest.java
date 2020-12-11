/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.protobuf.Message;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.validate.Validate.violationsOf;
import static io.spine.validate.given.MessageValidatorTestEnv.assertFieldPathIs;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class ValidationOfConstraintTest {

    public static final String VALIDATION_SHOULD = "Validation should ";

    private List<ConstraintViolation> violations;

    protected void validate(Message msg) {
        violations = violationsOf(msg);
    }

    protected ConstraintViolation firstViolation() {
        return violations.get(0);
    }

    protected ConstraintViolation singleViolation() {
        assertThat(violations).hasSize(1);
        return violations.get(0);
    }

    protected void assertValid(Message msg) {
        validate(msg);
        assertIsValid(true);
    }

    protected void assertNotValid(Message msg) {
        validate(msg);
        assertIsValid(false);
    }

    protected void assertNotValid(Message msg, boolean checkFieldPath) {
        validate(msg);
        assertIsValid(false, checkFieldPath);
    }

    protected void assertIsValid(boolean isValid) {
        assertIsValid(isValid, true);
    }

    protected void assertIsValid(boolean isValid, boolean checkFieldPath) {
        if (isValid) {
            assertThat(violations).isEmpty();
        } else {
            assertViolations(violations, checkFieldPath);
        }
    }

    private static void assertViolations(List<ConstraintViolation> violations,
                                         boolean checkFieldPath) {
        assertThat(violations)
                .isNotEmpty();
        for (ConstraintViolation violation : violations) {
            assertHasCorrectFormat(violation);
            if (checkFieldPath) {
                assertHasFieldPath(violation);
            }
        }
    }

    private static void assertHasCorrectFormat(ConstraintViolation violation) {
        String format = violation.getMsgFormat();
        assertFalse(format.isEmpty());
        boolean noParams = violation.getParamList()
                                    .isEmpty();
        if (noParams) {
            assertThat(format)
                    .doesNotContain("%s");
        } else {
            assertThat(format)
                    .contains("%s");
        }
    }

    private static void assertHasFieldPath(ConstraintViolation violation) {
        assertThat(violation.getFieldPath().getFieldNameList())
                .isNotEmpty();
    }

    protected void assertSingleViolation(Message message,
                                         String expectedErrMsg,
                                         String invalidFieldName) {
        assertNotValid(message);
        assertThat(violations)
                .hasSize(1);
        assertSingleViolation(expectedErrMsg, invalidFieldName);
    }

    /** Checks that a message is not valid and has a single violation. */
    protected void assertSingleViolation(String expectedErrMsg, String invalidFieldName) {
        ConstraintViolation violation = firstViolation();
        String actualErrorMessage = format(violation.getMsgFormat(), violation.getParamList()
                                                                              .toArray());
        assertThat(actualErrorMessage)
                .isEqualTo(expectedErrMsg);
        assertFieldPathIs(violation, invalidFieldName);
        assertThat(violation.getViolationList()).isEmpty();
    }

    protected void assertSingleViolation(Message message, String invalidFieldName) {
        assertNotValid(message);
        assertThat(violations)
             .hasSize(1);
        assertFieldPathIs(firstViolation(), invalidFieldName);
    }
}
