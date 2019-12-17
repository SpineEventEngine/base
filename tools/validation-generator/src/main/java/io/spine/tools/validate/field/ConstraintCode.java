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
import io.spine.logging.Logging;
import io.spine.tools.validate.AccumulateViolations;
import io.spine.tools.validate.FieldAccess;
import io.spine.tools.validate.MessageAccess;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.IsSet;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.FieldAccess.element;
import static io.spine.tools.validate.code.Blocks.empty;

/**
 * A validation constraint based on a Protobuf option of a message field.
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
     * Creates a new instance of {@code Builder} for {@code ConstraintCode} instances.
     *
     * @return new instance of {@code Builder}
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

        public Builder validateAsCollection() {
            this.forceSingular = true;
            return this;
        }

        public Builder validateOnlyIfSet() {
            this.onlyIfSet = true;
            return this;
        }

        /**
         * Creates a new instance of {@code ConstraintCode}.
         *
         * @return new instance of {@code ConstraintCode}
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

    private enum Cardinality {

        SINGULAR,
        COLLECTION
    }
}
