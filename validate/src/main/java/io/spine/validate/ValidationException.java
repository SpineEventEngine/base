/*
 * Copyright 2022, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.InlineMe;
import io.spine.base.ErrorWithMessage;
import io.spine.validate.diags.ViolationText;

import java.util.List;

/**
 * An exception that is thrown if a {@code Message} does not pass the validation.
 */
public class ValidationException
        extends RuntimeException implements ErrorWithMessage<ValidationError> {

    private static final long serialVersionUID = 0L;

    /**
     * List of the constraint violations that were found during the validation.
     */
    private final ImmutableList<ConstraintViolation> constraintViolations;

    /**
     * Creates a new instance with the given violations.
     */
    public ValidationException(Iterable<ConstraintViolation> violations) {
        super();
        this.constraintViolations = ImmutableList.copyOf(violations);
    }

    /**
     * Creates a new instance with the given violation.
     */
    public ValidationException(ConstraintViolation violation) {
        this(ImmutableList.of(violation));
    }

    @SuppressWarnings("unused" /* part of public API of the exception. */)
    public final List<ConstraintViolation> getConstraintViolations() {
        return constraintViolations;
    }

    /**
     * Provides the violation info as a {@link ValidationError}.
     *
     * @deprecated please use {@link #asMessage()}
     */
    @Deprecated
    @InlineMe(replacement = "this.asMessage()")
    public final ValidationError asValidationError() {
        return this.asMessage();
    }

    @Override
    public String getMessage() {
        return getClass().getSimpleName() + ": " + ViolationText.ofAll(constraintViolations);
    }

    /**
     * Provides the violation info as a {@link ValidationError}.
     */
    @Override
    public ValidationError asMessage() {
        return ValidationError.newBuilder()
                .addAllConstraintViolation(constraintViolations)
                .build();
    }
}
