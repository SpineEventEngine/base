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

package io.spine.tools.validate.field;

import com.squareup.javapoet.CodeBlock;
import io.spine.tools.validate.code.Expression;
import io.spine.validate.ConstraintViolation;

import java.util.Optional;
import java.util.function.Function;

/**
 * A factory of validation code for a field in a Protobuf message.
 */
public interface FieldValidatorFactory {

    /**
     * Generates validation code for the associated field.
     *
     * <p>After constructing a {@link ConstraintViolation}, yields the violation via an expression
     * produced by the {@code onViolation} function.
     *
     * @param onViolation
     *         a function which transforms the constraint violation into an {@link Expression} which
     *         stores the violation
     * @return the validation code or {@code Optional.empty()} if no validation is needed for
     *         the field
     */
    Optional<CodeBlock>
    generate(Function<Expression<ConstraintViolation>, Expression<?>> onViolation);

    /**
     * Obtains a boolean expression which checks if the field is set or not.
     *
     * @return expression which evaluates to {@code true} if the field is NOT set and {@code false}
     *         if the field is set
     */
    Expression<Boolean> isNotSet();
}
