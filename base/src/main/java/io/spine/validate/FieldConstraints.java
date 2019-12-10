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
import io.spine.validate.option.Required;
import io.spine.validate.option.Valid;
import io.spine.validate.option.ValidatingOptionFactory;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

final class FieldConstraints {

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    /**
     * Prevents the utility class instantiation.
     */
    private FieldConstraints() {
    }

    @SuppressWarnings("OverlyComplexMethod")
        // Assembles many options and option factories for all field types.
    static Stream<Constraint> of(FieldContext field) {
        checkNotNull(field);
        Required required = Required.create(false);
        Valid validate = new Valid();
        FieldDeclaration declaration = field.targetDeclaration();
        JavaType type = declaration.javaType();
        switch (type) {
            case INT:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forInt)
                                      .forField(field);
            case LONG:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forLong)
                                      .forField(field);
            case FLOAT:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forFloat)
                                      .forField(field);
            case DOUBLE:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forDouble)
                                      .forField(field);
            case BOOLEAN:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forBoolean)
                                      .forField(field);
            case STRING:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forString)
                                      .and(required)
                                      .forField(field);
            case BYTE_STRING:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forByteString)
                                      .and(required)
                                      .forField(field);
            case ENUM:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forEnum)
                                      .and(required)
                                      .forField(field);
            case MESSAGE:
                return ConstraintsFrom.factories(ValidatingOptionFactory::forMessage)
                                      .and(required, validate)
                                      .forField(field);
            default:
                log.atWarning().log("Unknown field type `%s` at `%s`.", type, declaration);
                return Stream.of();
        }
    }
}
