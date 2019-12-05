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
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.AccumulateViolations;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.GetterExpression;
import io.spine.tools.validate.code.NewViolation;
import io.spine.validate.Validate;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.option.OptionsProto.distinct;
import static io.spine.option.OptionsProto.required;
import static io.spine.tools.validate.code.BooleanExpression.fromCode;
import static io.spine.tools.validate.field.ContainerFields.isEmpty;
import static java.lang.String.format;
import static java.util.Optional.empty;

/**
 * A factory of validation code for {@code repeated} and {@code map} fields.
 */
final class CollectionFieldValidatorFactory implements FieldValidatorFactory {

    /**
     * The reference to an element in the validated field during iteration.
     */
    static final GetterExpression element = GetterExpression.of("element");

    private final FieldDeclaration field;
    private final Expression<?> fieldAccess;
    private final FieldValidatorFactory singular;
    private final boolean eachElementRequired;

    /**
     * Creates a new {@code CollectionFieldValidatorFactory}.
     *
     * @param field
     *         the declaration of the validated field
     * @param fieldAccess
     *         the {@link Expression} which evaluates into the field value
     * @param singular
     *         a {@link FieldValidatorFactory} for singular fields of this type; used to generate
     *         validation code for elements of the collection
     */
    CollectionFieldValidatorFactory(FieldDeclaration field,
                                    Expression<?> fieldAccess,
                                    FieldValidatorFactory singular) {
        this.field = checkNotNull(field);
        this.fieldAccess = checkNotNull(fieldAccess);
        this.singular = checkNotNull(singular);
        this.eachElementRequired = field.findOption(required) && singular.supportsRequired();
    }

    @Override
    public Optional<CodeBlock> generate(AccumulateViolations onViolation) {
        CodeBlock.Builder validation = CodeBlock.builder();
        boolean isRequired = field.findOption(required);
        if (isRequired) {
            validation.beginControlFlow("if ($L)", isEmpty(fieldAccess));
            collectionIsEmpty(onViolation, validation);
            validation.nextControlFlow("else");
        }
        addElementValidation(validation, onViolation);
        if (isRequired) {
            validation.endControlFlow();
        }
        addDuplicateCheck(validation, onViolation);
        return validation.isEmpty()
               ? empty()
               : Optional.of(validation.build());
    }

    private void
    collectionIsEmpty(AccumulateViolations onViolation, CodeBlock.Builder validation) {
        @SuppressWarnings("DuplicateStringLiteralInspection") // In generated code.
                NewViolation violation = violation()
                .setMessage("Collection must not be empty.")
                .build();
        validation.addStatement(onViolation.apply(violation).toCode());
    }

    @Override
    public BooleanExpression isNotSet() {
        return isEmpty(fieldAccess);
    }

    @Override
    public boolean supportsRequired() {
        return true;
    }

    private void addElementValidation(CodeBlock.Builder validation,
                                      AccumulateViolations onViolation) {
        Optional<CodeBlock> elementValidation = singular.generate(onViolation);
        Expression isNotSet = singular.isNotSet();
        if (elementValidation.isPresent() || eachElementRequired) {
            CodeBlock validationCode = elementValidation.orElse(CodeBlock.of(""));
            String isSet = field.name().javaCase() + "IsSet";
            if (eachElementRequired) {
                validation.addStatement("boolean $N = false", isSet);
            }
            String javaTypeName = field.isMap()
                                  ? field.valueDeclaration().javaTypeName()
                                  : field.javaTypeName();
            validation.beginControlFlow("for ($L $N: $L)",
                                        javaTypeName,
                                        element.toString(),
                                        fieldAccess.toCode());
            validation.add(validationCode);
            if (eachElementRequired) {
                validation.addStatement("$N |= !$L", isSet, isNotSet);
            }
            validation.endControlFlow();
            if (eachElementRequired) {
                @SuppressWarnings("DuplicateStringLiteralInspection") // In generated code.
                FieldConstraint fieldConstraint = new FieldConstraint(
                        fromCode("!$N", isSet),
                        NewViolation
                                .forField(field)
                                .setMessage("At least one element must be set.")
                                .build()
                );
                CodeBlock check = fieldConstraint.compile(onViolation);
                validation.add(check);
            }
        }
    }

    private void
    addDuplicateCheck(CodeBlock.Builder validation, AccumulateViolations onViolation) {
        if (field.findOption(distinct)) {
            FieldConstraint distinct = distinct();
            CodeBlock ruleCode = distinct.compile(onViolation);
            validation.add(ruleCode);
        }
    }

    private FieldConstraint distinct() {
        FieldConstraint rule = new FieldConstraint(
                fromCode("$T.containsDuplicates($L)", Validate.class, field),
                violation()
                        .setMessage(format("%s should not contain duplicates.", field))
                        .build()
        );
        return rule;
    }

    private NewViolation.Builder violation() {
        return NewViolation.forField(this.field);
    }
}
