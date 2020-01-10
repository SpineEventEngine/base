/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.validate;

import com.google.common.base.Objects;
import com.google.protobuf.Descriptors;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.BooleanExpression;
import io.spine.tools.validate.code.Expression;
import io.spine.validate.ExternalConstraints;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.code.BooleanExpression.isNull;
import static io.spine.tools.validate.code.Expression.formatted;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A boolean flag in the generated code which signifies whether or not a field has externally
 * defined constraints.
 *
 * <p>Such a flag is only generated for message fields marked with {@code (validate) = true}. Other
 * fields cannot be target for external constraints.
 */
final class ExternalConstraintFlag {

    private final FieldDeclaration declaration;
    private final Expression<@Nullable Boolean> field;

    ExternalConstraintFlag(FieldDeclaration field) {
        this.declaration = checkNotNull(field);
        this.field = formatted("is%sValidatedExternally", field.name()
                                                               .toCamelCase());
    }

    /**
     * Generated an expression which obtains the value of this flag.
     *
     * <p>The value of the flag must be set when this expression is used. Otherwise,
     * a {@code NullPointerException} is thrown.
     *
     * @return an expression of the primitive value of the flag
     */
    BooleanExpression value() {
        return BooleanExpression.fromCode("$L.booleanValue()", field);
    }

    /**
     * Generates code which assigns a value to the field by searching for the external constraints
     * in the {@linkplain ExternalConstraints global registry}.
     *
     * @return assignment code
     * @see ExternalConstraints#definedFor(Descriptors.Descriptor, String)
     */
    CodeBlock assignValue() {
        ClassName containingTypeName = ClassName.bestGuess(declaration.declaringType()
                                                                      .javaClassName()
                                                                      .toString());
        Expression<?> externallyValidated = BooleanExpression.fromCode(
                "$T.definedFor($T.getDescriptor(), $S)",
                ExternalConstraints.class,
                containingTypeName,
                declaration.name()
        );
        CodeBlock assignment = CodeBlock.of("$L = $L;", field, externallyValidated);
        return isNull(field).ifTrue(assignment)
                            .toCode();
    }

    /**
     * Obtains this flag as a {@link ClassMember}.
     */
    ClassMember asClassMember() {
        FieldSpec spec = FieldSpec
                .builder(Boolean.class, field.toString(), PRIVATE, STATIC)
                .build();
        return new Field(spec);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalConstraintFlag)) {
            return false;
        }
        ExternalConstraintFlag that = (ExternalConstraintFlag) o;
        return Objects.equal(declaration, that.declaration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(declaration);
    }
}
