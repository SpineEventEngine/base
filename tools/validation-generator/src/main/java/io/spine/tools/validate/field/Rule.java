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
import io.spine.tools.validate.ViolationTemplate;
import io.spine.tools.validate.code.Expression;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Rule {

    private final Function<Expression, Expression> condition;
    private final Function<Expression, ViolationTemplate> violationFactory;

    public Rule(Function<Expression, Expression> condition,
                Function<Expression, ViolationTemplate> violationFactory) {
        this.condition = checkNotNull(condition);
        this.violationFactory = checkNotNull(violationFactory);
    }

    Function<Expression, CodeBlock>
    compile(Function<ViolationTemplate, Expression> onViolation) {
        return field -> {
            ViolationTemplate violation = violationFactory.apply(field);
            CodeBlock ifViolation = onViolation.apply(violation)
                                               .toCode();
            CodeBlock fieldIsInvalid = condition.apply(field)
                                                .toCode();
            return CodeBlock
                    .builder()
                    .beginControlFlow("if($L)", fieldIsInvalid)
                    .addStatement(ifViolation)
                    .endControlFlow()
                    .build();
        };
    }
}
