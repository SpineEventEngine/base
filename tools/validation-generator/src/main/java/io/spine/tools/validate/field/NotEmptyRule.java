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

import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.ViolationTemplate;

import java.util.function.Function;

import static io.spine.tools.validate.code.Expression.formatted;

/**
 * Constructs the validation rule which checks if a value if {@code .isEmpty()}.
 */
final class NotEmptyRule {

    /**
     * Prevents the utility class instantiation.
     */
    private NotEmptyRule() {
    }

    /**
     * Creates a {@link Rule} which ensures that a value is not empty.
     */
    static Rule forField(ViolationTemplate.Builder violation) {
        Function<Expression<?>, Expression<Boolean>> condition = NotEmptyRule::isEmpty;
        @SuppressWarnings("DuplicateStringLiteralInspection") // Duplicates are in generated code.
        Function<Expression<?>, ViolationTemplate> violationFactory =
                field -> violation.setMessage("Field must be set.")
                                  .build();
        return new Rule(
                condition,
                violationFactory
        );
    }

    /**
     * Obtains the expression which calls {@code isEmpty()} method on the given {@code field}.
     */
    static Expression<Boolean> isEmpty(Expression<?> field) {
        return formatted("%s.isEmpty()", field);
    }
}
