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

import io.spine.tools.validate.ViolationTemplate;
import io.spine.tools.validate.code.Expression;

import java.util.function.Function;

import static io.spine.tools.validate.code.Expression.formatted;

final class NotEmptyRule {

    /**
     * Prevents the utility class instantiation.
     */
    private NotEmptyRule() {
    }

    static Rule forField(ViolationTemplate.Builder violation) {
        Function<Expression, Expression> condition = NotEmptyRule::isEmpty;
        @SuppressWarnings("DuplicateStringLiteralInspection") // Duplicates are in generated code.
                Function<Expression, ViolationTemplate> violationFactory =
                field -> violation.setMessage("Field must be set.")
                                  .build();
        return new Rule(
                condition,
                violationFactory
        );
    }

    static Expression isEmpty(Expression field) {
        return formatted("%s.isEmpty()", field);
    }
}
