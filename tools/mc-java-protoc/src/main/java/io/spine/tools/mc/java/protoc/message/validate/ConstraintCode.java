/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.protoc.message.validate;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.logging.Logging;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.mc.java.protoc.message.validate.FieldAccess.element;
import static io.spine.tools.mc.java.protoc.message.validate.Blocks.empty;
import static java.lang.System.lineSeparator;

/**
 * Conditional code which checks a validation constraint.
 */
public final class ConstraintCode implements Logging {

    private final Function<FieldAccess, CodeBlock> declarations;
    private final Check conditionCheck;
    private final CreateViolation createViolation;
    private final FieldAccess fieldAccess;
    private final Cardinality cardinality;
    private final AccumulateViolations onViolation;
    private final FieldDeclaration field;
    private final boolean onlyIfSet;

    private ConstraintCode(Builder builder) {
        this.declarations = builder.declarations;
        this.conditionCheck = builder.conditionCheck;
        this.createViolation = builder.createViolation;
        this.fieldAccess = builder.fieldAccess();
        this.cardinality = builder.cardinality();
        this.onViolation = builder.onViolation;
        this.onlyIfSet = builder.onlyIfSet;
        this.field = builder.field;
    }

    /**
     * Builds a {@link CodeBlock} which represents this constraint code.
     */
    public CodeBlock compile() {
        IsSet fieldIsSet = new IsSet(field);
        if (cardinality == Cardinality.SINGULAR) {
            return compileSingular(fieldIsSet, fieldAccess);
        } else {
            CodeBlock elementValidation = compileSingular(fieldIsSet, element);
            return CodeBlock
                    .builder()
                    .beginControlFlow("$L.forEach($N ->", fieldAccess, element.value())
                    .add(elementValidation)
                    .endControlFlow(")")
                    .build();
        }
    }

    private CodeBlock compileSingular(IsSet fieldIsSet, FieldAccess field) {
        CodeBlock ifViolation = onViolation.apply(createViolation.apply(field))
                                           .toCode();
        BooleanExpression condition = conditionCheck.apply(field);
        if (onlyIfSet) {
            BooleanExpression valueIsPresent = fieldIsSet.valueIsPresent(field);
            condition = valueIsPresent.and(condition);
        }
        return condition.isConstant()
               ? evaluateConstantCondition(condition, ifViolation)
               : evaluate(declarations.apply(field), condition, ifViolation);
    }

    private static CodeBlock evaluate(CodeBlock declarations,
                                      BooleanExpression condition,
                                      CodeBlock onViolation) {
        CodeBlock check = condition.ifTrue(onViolation)
                                   .toCode();
        return CodeBlock.builder()
                        .add(declarations)
                        .add(lineSeparator())
                        .add(check)
                        .build();
    }

    private CodeBlock
    evaluateConstantCondition(BooleanExpression condition, CodeBlock onViolation) {
        if (condition.isConstantTrue()) {
            _warn().log("Violation is always produced as validation check is a constant.");
            return onViolation;
        } else {
            return empty();
        }
    }

    /**
     * Creates a new builder for the instances of this type.
     *
     * @return new builder
     */
    public static Builder forField(FieldDeclaration field) {
        return new Builder(field);
    }

    /**
     * A builder for the {@code ConstraintCode} instances.
     */
    public static final class Builder {

        private final FieldDeclaration field;
        private MessageAccess messageAccess;
        private Function<FieldAccess, CodeBlock> declarations = f -> empty();
        private Check conditionCheck;
        private CreateViolation createViolation;
        private AccumulateViolations onViolation;
        private boolean forceSingular = false;
        private boolean onlyIfSet = false;

        private Builder(FieldDeclaration field) {
            this.field = checkNotNull(field);
        }

        public Builder messageAccess(MessageAccess messageAccess) {
            this.messageAccess = checkNotNull(messageAccess);
            return this;
        }

        /**
         * Sets a function which, given a field access expression, produces preliminary code.
         *
         * <p>The result of the preliminary code may be used by the condition and violation
         * expressions.
         */
        public Builder preparingDeclarations(Function<FieldAccess, CodeBlock> declarations) {
            this.declarations = checkNotNull(declarations);
            return this;
        }

        public Builder conditionCheck(Check conditionCheck) {
            this.conditionCheck = checkNotNull(conditionCheck);
            return this;
        }

        public Builder createViolation(CreateViolation createViolation) {
            this.createViolation = checkNotNull(createViolation);
            return this;
        }

        public Builder onViolation(AccumulateViolations onViolation) {
            this.onViolation = checkNotNull(onViolation);
            return this;
        }

        /**
         * Configures the built code to validate the field as a whole, even if it's a collection.
         */
        public Builder validateAsWhole() {
            this.forceSingular = true;
            return this;
        }

        /**
         * Configures the built code to run validation only on non-default values of the field.
         */
        public Builder validateOnlyIfSet() {
            this.onlyIfSet = true;
            return this;
        }

        /**
         * Creates a new instance of a {@link ConstraintCode}.
         */
        public ConstraintCode build() {
            checkNotNull(messageAccess);
            checkNotNull(conditionCheck);
            checkNotNull(createViolation);
            checkNotNull(onViolation);

            return new ConstraintCode(this);
        }

        private FieldAccess fieldAccess() {
            return messageAccess.get(field);
        }

        private Cardinality cardinality() {
            return field.isNotCollection() || forceSingular
                   ? Cardinality.SINGULAR
                   : Cardinality.COLLECTION;
        }
    }

    /**
     * Cardinality of a field.
     */
    private enum Cardinality {

        /**
         * A field has just a single value.
         */
        SINGULAR,

        /**
         * A field may have 0 to many values (a repeated field or a map).
         */
        COLLECTION
    }
}
