/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.validate;

import com.google.common.base.Optional;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.ClassName;
import io.spine.gradle.compiler.message.MessageTypeCache;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.gradle.compiler.message.fieldtype.FieldTypes.trimTypeName;
import static io.spine.gradle.compiler.message.fieldtype.ProtoScalarType.getBoxedScalarPrimitive;
import static io.spine.gradle.compiler.message.fieldtype.ProtoScalarType.getJavaTypeName;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utility class for working with the {@code ClassName}s.
 *
 * @author Illia Shepilov
 */
final class ClassNames {

    private static final ClassName STRING_CLASS_NAME = ClassName.get(String.class);

    /** Prevents instantiation of this utility class. */
    private ClassNames() {
    }

    /**
     * Returns the {@code ClassName} for the Protobuf field
     * based on the passed {@code FieldDescriptorProto}.
     *
     * @param fieldDescriptor  the field descriptor of the Protobuf field
     * @param messageTypeCache the cache of the message types
     * @return the obtained {@code ClassName}
     */
    static ClassName getParameterClassName(FieldDescriptorProto fieldDescriptor,
                                           MessageTypeCache messageTypeCache) {
        checkNotNull(fieldDescriptor);
        checkNotNull(messageTypeCache);

        String typeName = fieldDescriptor.getTypeName();
        if (typeName.isEmpty()) {
            return getJavaTypeForScalarType(fieldDescriptor);
        }
        typeName = trimTypeName(fieldDescriptor);
        final String parameterType = messageTypeCache.getCachedTypes()
                                                     .get(typeName);
        return ClassName.bestGuess(parameterType);
    }

    private static ClassName getJavaTypeForScalarType(FieldDescriptorProto fieldDescriptor) {
        final FieldDescriptorProto.Type fieldType = fieldDescriptor.getType();
        final String scalarType = getJavaTypeName(fieldType);
        try {
            final Optional<? extends Class<?>> scalarPrimitive = getBoxedScalarPrimitive(scalarType);
            if (scalarPrimitive.isPresent()) {
                return ClassName.get(scalarPrimitive.get());
            }
            return ClassName.get(Class.forName(scalarType));
        } catch (ClassNotFoundException ex) {
            final String exMessage = String.format("The class for the type: %s was not found.",
                                                   fieldDescriptor.getType());
            throw newIllegalArgumentException(exMessage, ex);
        }
    }

    /**
     * Returns the {@code ClassName} according to the specified package and class.
     *
     * @param javaPackage the package of the class
     * @param javaClass   the name of the class
     * @return the constructed {@code ClassName}
     */
    static ClassName getClassName(String javaPackage, String javaClass) {
        checkNotNull(javaPackage);
        checkNotNull(javaClass);
        final ClassName className = ClassName.get(javaPackage, javaClass);
        return className;
    }

    /**
     * Returns the {@code ClassName} for the generic parameter of the validating builder.
     *
     * @param javaPackage      the package of the class
     * @param messageTypeCache the cache of the message types
     * @param fieldName        the name of the field
     * @return the constructed {@code ClassName}
     * @throws IllegalArgumentException if the class of the validating builder is not found
     */
    static ClassName getValidatorMessageClassName(String javaPackage,
                                                  MessageTypeCache messageTypeCache,
                                                  String fieldName) {
        checkNotNull(javaPackage);
        checkNotNull(messageTypeCache);
        checkNotNull(fieldName);

        final Collection<String> values = messageTypeCache.getCachedTypes()
                                                          .values();
        final String expectedClassName = javaPackage + '.' + fieldName;
        for (String value : values) {
            if (value.equals(expectedClassName)) {
                return ClassName.get(javaPackage, fieldName);
            }
        }
        final String exMessage = String.format("The %s class is not found.", expectedClassName);
        throw newIllegalArgumentException(exMessage);
    }

    /**
     * Returns the {@code ClassName} for the {@code String} class.
     *
     * @return the constructed {@code ClassName}
     */
    static ClassName getStringClassName() {
        return STRING_CLASS_NAME;
    }
}
