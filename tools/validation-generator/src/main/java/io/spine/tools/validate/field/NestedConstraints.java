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
import io.spine.tools.validate.AccumulateViolations;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.ConditionalStatement;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.GetterExpression;
import io.spine.tools.validate.code.ViolationTemplate;
import io.spine.tools.validate.code.VoidExpression;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.Validate;

import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.code.BooleanExpression.fromCode;
import static io.spine.tools.validate.code.Expression.formatted;

/**
 * A validation rule which checks that a message field does not violate its own constraints.
 *
 * <p>If a field message is invalid, the message's {@link ConstraintViolation}s are wrapped into
 * a single violation and added to the rest of the violations of the top-level message.
 */
final class NestedConstraints implements Rule {

    private static final Type listOfViolations =
            new TypeToken<List<ConstraintViolation>>() {}.getType();

    private final FieldDeclaration field;
    private final GetterExpression fieldAccess;
    private final Expression<Iterable<ConstraintViolation>> violationsList;

    NestedConstraints(FieldDeclaration field, GetterExpression fieldAccess) {
        this.field = checkNotNull(field);
        this.fieldAccess = checkNotNull(fieldAccess);
        this.violationsList = formatted("%sViolations", field.name().javaCase());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Produces validation code for checking constraints of a message field.
     *
     * <p>Violations for the field are obtained via {@link Validate#violationsOf}.
     * @return
     */
    @Override
    public CodeBlock
    compile(AccumulateViolations onViolation, CodeBlock orElse) {
        return CodeBlock
                .builder()
                .addStatement("$T $N = $T.violationsOf($L)",
                              listOfViolations,
                              violationsList.toString(),
                              Validate.class,
                              fieldAccess)
                .add(violationsCheck(onViolation, orElse))
                .build();
    }

    private CodeBlock violationsCheck(AccumulateViolations onViolation, CodeBlock orElse) {
        BooleanExpression notDefault =
                fromCode("$T.isNotDefault($L)", Validate.class, fieldAccess);
        CodeBlock validation = collectViolations(onViolation, orElse);
        return notDefault.ifTrue(validation).toCode();
    }

    private CodeBlock collectViolations(AccumulateViolations onViolation, CodeBlock orElse) {
        BooleanExpression notEmptyViolations =
                fromCode("!$N.isEmpty()", violationsList.toString());
        VoidExpression violationHandler = onViolation.apply(violationExpression());
        ConditionalStatement violationsArePresent =
                notEmptyViolations.ifTrue(violationHandler.toCode());
        return violationsArePresent.orElse(orElse);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private ViolationTemplate violationExpression() {
        return ViolationTemplate
                .forField(field)
                .setMessage("Message must have valid fields.")
                .setNestedViolations(violationsList)
                .build();
    }
}
