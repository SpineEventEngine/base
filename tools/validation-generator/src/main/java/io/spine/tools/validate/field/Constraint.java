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
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.Expression;
import io.spine.validate.ConstraintViolation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A validation constraint based on a Protobuf option.
 */
final class Constraint implements Rule {

    private final BooleanExpression condition;
    private final Expression<ConstraintViolation> violation;

    /**
     * Creates a new {@code Rule}.
     *  @param condition
     *         a function which accepts the field value and yields a boolean expression which
     *         evaluates into {@code true} when the rule is broken
     * @param violation
     *         a function which accepts the field and yields an expression which evaluates into
     *         a {@link ConstraintViolation} which describes the broken rule
     */
    Constraint(BooleanExpression condition, Expression<ConstraintViolation> violation) {
        this.condition = checkNotNull(condition);
        this.violation = checkNotNull(violation);
    }

    @Override
    public CodeBlock compile(AccumulateViolations onViolation, CodeBlock orElse) {
        CodeBlock ifViolation = onViolation.apply(this.violation)
                                           .toCode();
        return condition.ifTrue(ifViolation)
                        .orElse(orElse);
    }
}
