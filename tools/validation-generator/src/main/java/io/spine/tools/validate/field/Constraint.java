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
import io.spine.tools.validate.AccumulateViolations;
import io.spine.validate.ConstraintViolation;

/**
 * A message validation constraint.
 *
 * <p>A {@code Constraint} may represent one or several validation  applied to a message. In
 * general, a notion of being followed or broken is present for a rule.
 */
public interface Constraint {

    /**
     * Compiles this rule into the Java validation code.
     *
     * <p>If the rule is broken, one or more {@link ConstraintViolation}s are passed to
     * the {@link AccumulateViolations}.
     *
     * @param onViolation
     *         a function which accept a {@link ConstraintViolation} and yields it to where the
     *         violations are accumulated for the validated message
     * @return a function which accepts the field value and returns the validation code
     */
    default CodeBlock compile(AccumulateViolations onViolation) {
        return compile(onViolation, CodeBlock.of(""));
    }

    /**
     * Compiles this constraint into the Java validation code.
     *
     * <p>If the constraint is violated, one or more {@link ConstraintViolation}s are passed to
     * the {@link AccumulateViolations}. If the message follows the rule, no violations are produced
     * and the {@code orElse} code block is executed.
     *
     * @param onViolation
     *         a function which accept a {@link ConstraintViolation} and yields it to where the
     *         violations are accumulated for the validated message
     * @param orElse
     *         if the rule does not add a violation, this code will be invoked
     * @return a function which accepts the field value and returns the validation code
     */
    CodeBlock compile(AccumulateViolations onViolation, CodeBlock orElse);
}
