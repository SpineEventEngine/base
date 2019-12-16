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
import io.spine.tools.validate.FieldAccess;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.Expression;
import io.spine.validate.ConstraintViolation;

public final class CollectionConstraintCode implements ConstraintCode {

    private final CodeBlock declarations;
    private final BooleanExpression condition;
    private final Expression<ConstraintViolation> violation;
    private final FieldAccess fieldAccess;

    public CollectionConstraintCode(CodeBlock declarations,
                                    BooleanExpression condition,
                                    Expression<ConstraintViolation> violation,
                                    FieldAccess fieldAccess) {
        this.declarations = declarations;
        this.condition = condition;
        this.violation = violation;
        this.fieldAccess = fieldAccess;
    }

    @Override
    public CodeBlock compile(AccumulateViolations onViolation, CodeBlock orElse) {
        FieldConstraintCode singular = new FieldConstraintCode(declarations, condition, violation);

        String varName = fieldAccess.validatableValue()
                                    .value();
        return CodeBlock
                .builder()
                .beginControlFlow("$L.forEach($N ->", fieldAccess.toCode(), varName)
                .add(singular.compile(onViolation, orElse))
                .endControlFlow(")")
                .build();
    }
}
