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
import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.ViolationTemplate;
import io.spine.validate.ConstraintViolation;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.option.OptionsProto.required;
import static io.spine.tools.validate.field.FieldCardinality.SINGULAR;

/**
 * The implementation base for {@link FieldValidatorFactory}s for singular (not repeated) fields.
 */
abstract class SingularFieldValidatorFactory implements FieldValidatorFactory {

    private final FieldDeclaration field;
    private final Expression fieldAccess;
    private final FieldCardinality cardinality;

    /**
     * Creates a new {@code SingularFieldValidatorFactory}.
     *
     * @param field
     *         the declaration of the field to validate
     * @param fieldAccess
     *         the value of the validated field
     * @param cardinality
     *         whether or not the value is an element of a repeated field
     */
    SingularFieldValidatorFactory(FieldDeclaration field,
                                  Expression fieldAccess,
                                  FieldCardinality cardinality) {
        this.field = checkNotNull(field);
        this.fieldAccess = checkNotNull(fieldAccess);
        this.cardinality = checkNotNull(cardinality);
    }

    /**
     * Creates the validation {@link Constraint}s to generate code for.
     */
    protected abstract ImmutableList<Rule> rules();

    @Override
    public Optional<CodeBlock>
    generate(Function<Expression<ConstraintViolation>, Expression<?>> onViolation) {
        CodeBlock code = CodeBlock.of("");
        for (Rule rule : rules().reverse()) {
            code = rule.compile(onViolation, code)
                       .apply(fieldAccess);
        }
        return code.isEmpty()
               ? Optional.empty()
               : Optional.of(code);
    }

    final boolean isRequired() {
        return field.findOption(required) && cardinality == SINGULAR;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // In generated code.k
    final Constraint requiredRule() {
        return new Constraint(field -> isNotSet(),
                              field -> violationTemplate()
                                      .setMessage("Field must be set.")
                                      .build());
    }

    final FieldDeclaration field() {
        return field;
    }

    final Expression fieldAccess() {
        return fieldAccess;
    }

    final ViolationTemplate.Builder violationTemplate() {
        return ViolationTemplate.forField(field);
    }

    @Override
    public boolean supportsRequired() {
        return true;
    }
}
