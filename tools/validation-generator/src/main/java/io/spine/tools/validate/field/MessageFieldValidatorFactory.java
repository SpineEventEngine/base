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

import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.Message;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.protobuf.Messages;
import io.spine.tools.validate.code.Expression;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.Validate;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.spine.option.OptionsProto.required;
import static io.spine.option.OptionsProto.validate;
import static io.spine.tools.validate.code.Expression.formatted;

/**
 * A {@link FieldValidatorFactory} for message and enum fields.
 */
final class MessageFieldValidatorFactory extends SingularFieldValidatorFactory {

    MessageFieldValidatorFactory(FieldDeclaration field,
                                 Expression fieldAccess,
                                 FieldCardinality cardinality) {
        super(field, fieldAccess, cardinality);
    }

    @Override
    public Optional<CodeBlock>
    generate(Function<Expression<ConstraintViolation>, Expression<?>> onViolation) {
        CodeBlock.Builder code = super.generate(onViolation)
                                      .map(CodeBlock::toBuilder)
                                      .orElseGet(CodeBlock::builder);
        if (field().isMessage() && field().findOption(validate)) {
            boolean optional = !field().findOption(required);
            if (optional) {
                code.beginControlFlow("if ($T.isNotDefault($L))", Messages.class, fieldAccess());
            }
            validateRecursively(code, onViolation);
            if (optional) {
                code.endControlFlow();
            }
        }
        return code.isEmpty()
               ? Optional.empty()
               : Optional.of(code.build());
    }

    @Override
    protected ImmutableList<Rule> rules() {
        return isRequired()
               ? ImmutableList.of(requiredRule())
               : ImmutableList.of();
    }

    /**
     * Validates the message field according to the constraints of the message.
     *
     * @param code
     *         the validation code to append to
     * @param onViolation
     *         violation transformer function
     * @see Validate#violations(Message)
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private void
    validateRecursively(CodeBlock.Builder code,
                        Function<Expression<ConstraintViolation>, Expression<?>> onViolation) {
        Type listOfViolations = new TypeToken<List<ConstraintViolation>>() {}.getType();
        String varName = field().name().javaCase() + "Violations";
        code.addStatement("$T $N = $T.violations($L)",
                          listOfViolations,
                          varName,
                          Validate.class,
                          fieldAccess());
        code.beginControlFlow("if (!$N.isEmpty())", varName);
        Expression<ConstraintViolation> violationExpression = violationTemplate()
                .setMessage("Message must have valid fields.")
                .setNestedViolations(Expression.of(varName))
                .build();
        Expression violationHandler = onViolation.apply(violationExpression);
        code.addStatement(violationHandler.toCode());
        code.endControlFlow();
    }

    @Override
    public Expression<Boolean> isNotSet() {
        CodeBlock isDefaultCall = CodeBlock.of("$T.isDefault", ClassName.get(Messages.class));
        return formatted("%s(%s)", isDefaultCall, fieldAccess());
    }
}
