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
import io.spine.tools.validate.ViolationTemplate;
import io.spine.tools.validate.code.Expression;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractFieldValidatorFactory implements FieldValidatorFactory {

    private final FieldDeclaration field;
    private final Expression fieldAccess;
    private final FieldCardinality cardinality;

    AbstractFieldValidatorFactory(FieldDeclaration field,
                                  Expression fieldAccess,
                                  FieldCardinality cardinality) {
        this.field = checkNotNull(field);
        this.fieldAccess = checkNotNull(fieldAccess);
        this.cardinality = checkNotNull(cardinality);
    }

    protected abstract ImmutableList<Rule> rules();

    @Override
    public Optional<CodeBlock> generate(Function<ViolationTemplate, Expression> onViolation) {
        CodeBlock code = rules()
                .stream()
                .map(rule -> rule.compile(onViolation))
                .reduce(CodeBlock.builder(),
                        (builder, ruleFunction) -> builder.add(ruleFunction.apply(fieldAccess)),
                        (l, r) -> l.add(r.build()))
                .build();
        return code.isEmpty()
               ? Optional.empty()
               : Optional.of(code);
    }

    protected final Rule requiredRule() {
        return new Rule(field -> isNotSet(),
                        field -> violationTemplate()
                                .setMessage("Field must be set.")
                                .build());
    }

    protected final FieldDeclaration field() {
        return field;
    }

    protected final FieldCardinality cardinality() {
        return cardinality;
    }

    protected final Expression fieldAccess() {
        return fieldAccess;
    }

    ViolationTemplate.Builder violationTemplate() {
        return ViolationTemplate.forField(field);
    }
}
