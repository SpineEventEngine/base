/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import io.spine.validate.diags.ViolationText;

import java.util.List;

/**
 * An exception that is thrown if a {@code Message} does not pass the validation.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    /**
     * List of the constraint violations that were found during the validation.
     */
    private final ImmutableList<ConstraintViolation> constraintViolations;

    public ValidationException(Iterable<ConstraintViolation> violations) {
        super();
        this.constraintViolations = ImmutableList.copyOf(violations);
    }

    @SuppressWarnings("unused" /* part of public API of the exception. */)
    public final List<ConstraintViolation> getConstraintViolations() {
        return constraintViolations;
    }

    /**
     * Provides the violation info as a {@link ValidationError}.
     */
    public final ValidationError asValidationError() {
        return ValidationError
                .newBuilder()
                .addAllConstraintViolation(constraintViolations)
                .build();
    }

    @Override
    public String getMessage() {
        return getClass().getSimpleName() + ": " + ViolationText.ofAll(constraintViolations);
    }
}
