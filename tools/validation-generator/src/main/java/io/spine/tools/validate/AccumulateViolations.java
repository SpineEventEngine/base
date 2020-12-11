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

package io.spine.tools.validate;

import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.VoidExpression;
import io.spine.validate.ConstraintViolation;

import java.util.function.Function;

/**
 * A function which accepts an expression of a {@link ConstraintViolation} and transforms it into
 * an expression of the violation being saved.
 *
 * <p>Typically, one accumulator is used many times for different violations.
 *
 * <p>For example:
 * <pre>{@code
 * AccumulateViolation accumulator =
 *     (violationExpression) -> formatted("errorBuilder.addViolation(%s);", violationExpression);
 * }</pre>
 *
 * <p>In the example above a function takes a {@link ConstraintViolation} expression and uses it
 * to add the violation to a {@code ValidationError} builder.
 */
@FunctionalInterface
public interface AccumulateViolations
        extends Function<Expression<ConstraintViolation>, VoidExpression> {
}
