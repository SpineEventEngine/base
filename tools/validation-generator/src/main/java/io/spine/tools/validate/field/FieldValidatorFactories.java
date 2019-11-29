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

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.squareup.javapoet.CodeBlock;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.ViolationAccumulator;
import io.spine.tools.validate.code.Expression;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.code.Expression.formatted;
import static io.spine.tools.validate.field.FieldCardinality.REPEATED;
import static io.spine.tools.validate.field.FieldCardinality.SINGULAR;
import static java.util.Optional.empty;

/**
 * A factory of {@link FieldValidatorFactory}s.
 */
public final class FieldValidatorFactories {

    private final Expression<?> messageAccess;

    /**
     * Creates a new {@code FieldValidatorFactories}.
     *
     * @param messageAccess an expression which evaluates into the validated message value
     */
    public FieldValidatorFactories(Expression<?> messageAccess) {
        this.messageAccess = checkNotNull(messageAccess);
    }

    /**
     * Creates a new {@link FieldValidatorFactory} for the given field.
     *
     * @param field
     *         the field to validate
     * @return validator factory
     */
    public FieldValidatorFactory forField(FieldDeclaration field) {
        return field.isCollection()
               ? forCollections(field)
               : forSingularField(field);
    }

    private FieldValidatorFactory forCollections(FieldDeclaration field) {
        FieldValidatorFactory singularFactory =
                forSingularField(field, REPEATED, CollectionFieldValidatorFactory.element);
        String suffix = field.isMap() ? "Map().values()" : "List()";
        Expression fieldAccess =
                formatted("%s.get%s%s", messageAccess, field.name().toCamelCase(), suffix);
        return new CollectionFieldValidatorFactory(field, fieldAccess, singularFactory);
    }

    private FieldValidatorFactory
    forSingularField(FieldDeclaration field) {
        Expression fieldAccess =
                formatted("%s.get%s()", messageAccess, field.name().toCamelCase());
        return forSingularField(field, SINGULAR, fieldAccess);
    }

    private static FieldValidatorFactory
    forSingularField(FieldDeclaration field, FieldCardinality cardinality, Expression fieldAccess) {
        JavaType type = field.isMap()
                        ? field.valueDeclaration().javaType()
                        : field.javaType();
        switch (type) {
            case STRING:
                return new StringFieldValidatorFactory(field, fieldAccess, cardinality);
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return new NumberFieldValidatorFactory(field, type, fieldAccess, cardinality);
            case ENUM:
            case MESSAGE:
                return new MessageFieldValidatorFactory(field, fieldAccess, cardinality);
            case BYTE_STRING:
                return new ByteStringFieldValidatorFactory(field, fieldAccess, cardinality);
            case BOOLEAN:
            default:
                return NoOpFactory.INSTANCE;
        }
    }

    /**
     * A {@link FieldValidatorFactory} which never produces any field validation.
     */
    private enum NoOpFactory implements FieldValidatorFactory {

        INSTANCE;

        @Override
        public Optional<CodeBlock> generate(ViolationAccumulator onViolation) {
            return empty();
        }

        @Override
        public Expression<Boolean> isNotSet() {
            return Expression.of(String.valueOf(false));
        }

        @Override
        public boolean supportsRequired() {
            return false;
        }
    }
}
