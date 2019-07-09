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

package io.spine.base;

import io.spine.testing.UtilityClassTest;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableList;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Errors.causeOf;
import static io.spine.base.Errors.fromThrowable;
import static io.spine.base.Identifier.newUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Errors utility class should")
class ErrorsTest extends UtilityClassTest<Errors> {

    ErrorsTest() {
        super(Errors.class);
    }

    @Test
    @DisplayName("convert cause of throwable to Error")
    void convertCauseOfThrowableToError() {
        int errorCode = 404;
        String errorMessage = newUuid();

        // A Throwable with cause.
        RuntimeException throwable = new IllegalStateException(
                new RuntimeException(errorMessage)
        );

        Error error = causeOf(throwable, errorCode);

        assertEquals(errorCode, error.getCode());
        assertEquals(errorMessage, error.getMessage());
    }

    @Test
    @DisplayName("convert throwable to Error")
    void convertThrowableToError() {
        String errorMessage = newUuid();
        RuntimeException throwable = new RuntimeException(errorMessage);

        Error error = fromThrowable(throwable);

        assertEquals(errorMessage, error.getMessage());
    }

    @Test
    @DisplayName("convert ValidationException into an error with validation_error")
    void validation() {
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .build();
        ValidationException exception = new ValidationException(ImmutableList.of(violation));
        Error error = fromThrowable(exception);
        assertThat(error.getValidationError()).isEqualTo(exception.asValidationError());
    }
}
