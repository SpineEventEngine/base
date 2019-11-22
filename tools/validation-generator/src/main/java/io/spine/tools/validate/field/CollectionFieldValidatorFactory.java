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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.ViolationTemplate;
import io.spine.tools.validate.code.Expression;
import io.spine.validate.Validate;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.squareup.javapoet.ClassName.bestGuess;
import static io.spine.option.OptionsProto.distinct;
import static io.spine.option.OptionsProto.required;
import static io.spine.tools.validate.code.Expression.formatted;
import static java.lang.String.format;
import static java.util.Optional.empty;

final class CollectionFieldValidatorFactory implements FieldValidatorFactory {

    static final Expression element = Expression.of("element");

    private final FieldDeclaration field;
    private final Expression fieldAccess;
    private final FieldValidatorFactory singular;
    private final boolean isRequired;

    CollectionFieldValidatorFactory(FieldDeclaration field,
                                    Expression fieldAccess,
                                    FieldValidatorFactory singular) {
        this.field = checkNotNull(field);
        this.fieldAccess = checkNotNull(fieldAccess);
        this.singular = checkNotNull(singular);
        this.isRequired = field.findOption(required);
    }

    @Override
    public Optional<CodeBlock> generate(Function<ViolationTemplate, Expression> onViolation) {
        CodeBlock.Builder validation = CodeBlock.builder();
        addCollectionValidation(validation, onViolation);
        addElementValidation(validation, onViolation);
        return validation.isEmpty()
               ? empty()
               : Optional.of(validation.build());
    }

    @Override
    public Expression isNotSet() {
        return NotEmptyRule.isEmpty(fieldAccess);
    }

    private void addElementValidation(CodeBlock.Builder validation,
                                      Function<ViolationTemplate, Expression> onViolation) {
        Optional<CodeBlock> elementValidation = singular.generate(onViolation);
        Expression isNotSet = singular.isNotSet();
        if (elementValidation.isPresent() || isRequired) {
            CodeBlock validationCode = elementValidation.orElse(CodeBlock.of(""));
            String notSet = "notSet";
            validation.addStatement("boolean $N = false", notSet);
            validation.beginControlFlow("for ($T $N: $L)",
                                        bestGuess(field.javaTypeName()),
                                        element.value(),
                                        fieldAccess.value());
            validation.add(validationCode);
            validation.addStatement("$N |= $L", notSet, isNotSet);
            validation.endControlFlow();
            if (isRequired) {
                Rule rule = new Rule(field -> Expression.of(notSet),
                                     field -> ViolationTemplate
                                             .forField(this.field)
                                             .setMessage("At least one element must be set.")
                                             .build());
                CodeBlock check = rule.compile(onViolation)
                                      .apply(fieldAccess);
                validation.add(check);
            }
        }
    }

    private void addCollectionValidation(CodeBlock.Builder validation,
                                         Function<ViolationTemplate, Expression> onViolation) {
        if (isRequired) {
            Rule required = NotEmptyRule.forField(violation());
            append(validation, required, onViolation);
        }
        if (field.findOption(distinct)) {
            Rule distinct = distinct();
            append(validation, distinct, onViolation);
        }
    }

    private void append(CodeBlock.Builder code,
                        Rule validation,
                        Function<ViolationTemplate, Expression> onViolation) {
        Function<Expression, CodeBlock> ruleFactory = validation.compile(onViolation);
        CodeBlock ruleCode = ruleFactory.apply(fieldAccess);
        code.add(ruleCode);
    }

    private Rule distinct() {
        CodeBlock isDefaultCall =
                CodeBlock.of("$T.containsDuplicates", ClassName.get(Validate.class));
        Rule rule = new Rule(field -> formatted("%s(%s)", isDefaultCall, field),
                             field -> violation()
                                     .setMessage(format("%s should not contain duplicates.", field))
                                     .build());
        return rule;
    }

    private ViolationTemplate.Builder violation() {
        return ViolationTemplate.forField(this.field);
    }
}
