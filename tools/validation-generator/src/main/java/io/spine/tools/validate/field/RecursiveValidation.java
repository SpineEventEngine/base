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

import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.ViolationTemplate;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.Validate;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class RecursiveValidation implements Rule {

    private final FieldDeclaration field;

    RecursiveValidation(FieldDeclaration field) {
        this.field = checkNotNull(field);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Override
    public Function<Expression<?>, CodeBlock>
    compile(Function<Expression<ConstraintViolation>, Expression<?>> onViolation,
            CodeBlock orElse) {
        return fieldAccess -> {
            CodeBlock.Builder code = CodeBlock.builder();
            code.beginControlFlow("if ($T.isNotDefault($L))", Validate.class, fieldAccess);
            Type listOfViolations = new TypeToken<List<ConstraintViolation>>() {}.getType();
            String varName = field.name().javaCase() + "Violations";
            code.addStatement("$T $N = $T.violations($L)",
                              listOfViolations,
                              varName,
                              Validate.class,
                              fieldAccess);
            code.beginControlFlow("if (!$N.isEmpty())", varName);
            Expression<ConstraintViolation> violationExpression = ViolationTemplate
                    .forField(field)
                    .setMessage("Message must have valid fields.")
                    .setNestedViolations(Expression.of(varName))
                    .build();
            Expression violationHandler = onViolation.apply(violationExpression);
            code.addStatement(violationHandler.toCode());
            if (!orElse.isEmpty()) {
                code.nextControlFlow("else");
                code.add(orElse);
            }
            return code
                    .endControlFlow()
                    .endControlFlow()
                    .build();
        };
    }
}
