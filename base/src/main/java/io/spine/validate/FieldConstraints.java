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

package io.spine.validate;

import com.google.common.flogger.FluentLogger;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.validate.option.Distinct;
import io.spine.validate.option.FieldValidatingOption;
import io.spine.validate.option.Goes;
import io.spine.validate.option.Required;
import io.spine.validate.option.Valid;
import io.spine.validate.option.ValidatingOptionFactory;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A factory of field validation {@link Constraint}s.
 */
final class FieldConstraints {

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    /**
     * Prevents the utility class instantiation.
     */
    private FieldConstraints() {
    }

    /**
     * Assembles {@link Constraint}s for a given field.
     *
     * @param field
     *         field to validate
     * @return validation constraints
     */
    @SuppressWarnings("OverlyComplexMethod")
        // Assembles many options and option factories for all field types.
    static Stream<Constraint> of(FieldContext field) {
        checkNotNull(field);
        FieldDeclaration declaration = field.targetDeclaration();
        JavaType type = declaration.javaType();
        switch (type) {
            case INT:
                return primitive(ValidatingOptionFactory::forInt, field);
            case LONG:
                return primitive(ValidatingOptionFactory::forLong, field);
            case FLOAT:
                return primitive(ValidatingOptionFactory::forFloat, field);
            case DOUBLE:
                return primitive(ValidatingOptionFactory::forDouble, field);
            case BOOLEAN:
                return primitive(ValidatingOptionFactory::forBoolean, field);
            case STRING:
                return objectLike(ValidatingOptionFactory::forString, field);
            case BYTE_STRING:
                return objectLike(ValidatingOptionFactory::forByteString, field);
            case ENUM:
                return objectLike(ValidatingOptionFactory::forEnum, field);
            case MESSAGE:
                return ConstraintsFrom
                        .factories(ValidatingOptionFactory::forMessage)
                        .and(Required.create(false), Goes.create(), new Valid())
                        .andForCollections(Distinct.create())
                        .forField(field);
            default:
                log.atWarning().log("Unknown field type `%s` at `%s`.", type, declaration);
                return Stream.of();
        }
    }

    private static Stream<Constraint>
    primitive(Function<ValidatingOptionFactory, Set<FieldValidatingOption<?>>> selector,
              FieldContext context) {
        return ConstraintsFrom
                .factories(selector)
                .andForCollections(Required.create(false),
                                   Goes.create(),
                                   Distinct.create())
                .forField(context);

    }

    private static Stream<Constraint>
    objectLike(Function<ValidatingOptionFactory, Set<FieldValidatingOption<?>>> selector,
               FieldContext context) {
        return ConstraintsFrom
                .factories(selector)
                .and(Required.create(false), Goes.create())
                .andForCollections(Distinct.create())
                .forField(context);
    }
}
