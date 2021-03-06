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

package io.spine.base;

import com.google.common.collect.ImmutableList;
import io.spine.testing.UtilityClassTest;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Errors.causeOf;
import static io.spine.base.Errors.fromThrowable;
import static io.spine.base.Identifier.newUuid;
import static io.spine.testing.TestValues.randomString;

@DisplayName("Errors utility class should")
class ErrorsTest extends UtilityClassTest<Errors> {

    ErrorsTest() {
        super(Errors.class);
    }

    @Nested
    @DisplayName("convert cause of throwable to `Error`")
    class GetCause {

        private Throwable cause;
        private Throwable throwable;

        @BeforeEach
        void createExceptions() {
            String causeMessage = randomString();
            cause = new RuntimeException(causeMessage);
            throwable = new IllegalStateException(cause);
        }

        @Test
        @DisplayName("with error code")
        void withCode() {
            int errorCode = 404;
            Error error = causeOf(throwable, errorCode);

            assertHasCause(error);
            assertThat(error.getCode())
                    .isEqualTo(errorCode);
        }

        @Test
        @DisplayName("without error code")
        void withoutCode() {
            Error error = causeOf(throwable);

            assertHasCause(error);
        }

        private void assertHasCause(Error error) {
            assertThat(error.getMessage())
                    .isEqualTo(cause.getMessage());
        }
    }


    @Test
    @DisplayName("convert throwable to `Error`")
    void convertThrowableToError() {
        String expected = newUuid();
        RuntimeException throwable = new RuntimeException(expected);

        Error error = fromThrowable(throwable);

        assertThat(error.getMessage())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("convert `ValidationException` into an error with `validation_error`")
    void validation() {
        ConstraintViolation violation = ConstraintViolation.newBuilder().build();
        ValidationException exception = new ValidationException(ImmutableList.of(violation));
        Error error = fromThrowable(exception);
        assertThat(error.getValidationError())
                .isEqualTo(exception.asValidationError());
    }
}
