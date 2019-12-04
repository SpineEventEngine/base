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
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.code.GetterExpression;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.code.GetterExpression.mapField;
import static io.spine.tools.validate.code.GetterExpression.repeatedField;
import static io.spine.tools.validate.code.GetterExpression.singularField;
import static io.spine.tools.validate.field.FieldCardinality.REPEATED;
import static io.spine.tools.validate.field.FieldCardinality.SINGULAR;

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
        GetterExpression getter = field.isMap()
                                  ? mapField(messageAccess, field.name())
                                  : repeatedField(messageAccess, field.name());
        return new CollectionFieldValidatorFactory(field, getter, singularFactory);
    }

    private FieldValidatorFactory
    forSingularField(FieldDeclaration field) {
        GetterExpression fieldAccess = singularField(messageAccess, field.name());
        return forSingularField(field, SINGULAR, fieldAccess);
    }

    private static FieldValidatorFactory
    forSingularField(FieldDeclaration field,
                     FieldCardinality cardinality,
                     GetterExpression fieldAccess) {
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
}
