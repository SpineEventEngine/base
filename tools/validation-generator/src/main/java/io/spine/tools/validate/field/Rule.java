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

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A validation rule.
 */
final class Rule {

    private final Function<? super Expression<?>, ? extends Expression<Boolean>> condition;
    private final
    Function<? super Expression<?>, ? extends Expression<ConstraintViolation>> violationFactory;

    /**
     * Creates a new {@code Rule}.
     *
     * @param condition
     *         a function which accepts the field value and yields a boolean expression which
     *         evaluates into {@code true} when the rule is broken
     * @param violationFactory
     *         a function which accepts the field and yields an expression which evaluates into
     *         a {@link ConstraintViolation} which describes the broken rule
     */
    Rule(Function<? super Expression<?>, ? extends Expression<Boolean>> condition,
         Function<? super Expression<?>, ? extends Expression<ConstraintViolation>> violationFactory) {
        this.condition = checkNotNull(condition);
        this.violationFactory = checkNotNull(violationFactory);
    }

    /**
     * Compiles this rule into the Java validation code.
     *
     * @param onViolation
     *         a function which accept a {@link ConstraintViolation} and yields it to where the
     *         violations are accumulated for the validated message
     * @return a function which accepts the field value and returns the validation code
     */
    Function<Expression<?>, CodeBlock>
    compile(Function<Expression<ConstraintViolation>, Expression<?>> onViolation) {
        return field -> {
            Expression<ConstraintViolation> violation = violationFactory.apply(field);
            CodeBlock fieldIsInvalid = condition.apply(field)
                                                .toCode();
            CodeBlock ifViolation = onViolation.apply(violation)
                                               .toCode();
            return CodeBlock
                    .builder()
                    .beginControlFlow("if ($L)", fieldIsInvalid)
                    .addStatement(ifViolation)
                    .endControlFlow()
                    .build();
        };
    }
}
