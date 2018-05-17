/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.validation;

import com.google.common.base.Optional;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.squareup.javapoet.ClassName;
import io.spine.tools.compiler.MessageTypeCache;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.fieldtype.FieldTypes.trimTypeName;
import static io.spine.tools.java.PrimitiveType.getWrapperClass;
import static io.spine.tools.proto.ScalarType.getJavaTypeName;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;

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
     * Returns the {@code ClassName} for the Protobuf field.
     *
     * @param field the field descriptor of the Protobuf field
     * @param cache the cache of the message types
     * @return the obtained {@code ClassName}
     */
    static ClassName getParameterClassName(FieldDescriptorProto field, MessageTypeCache cache) {
        checkNotNull(field);
        checkNotNull(cache);

        String typeName = field.getTypeName();
        if (typeName.isEmpty()) {
            return getJavaTypeForScalarType(field);
        }
        typeName = trimTypeName(field);
        final String parameterType = cache.getCachedTypes()
                                          .get(typeName);
        return ClassName.bestGuess(parameterType);
    }

    private static ClassName getJavaTypeForScalarType(FieldDescriptorProto field) {
        final FieldDescriptorProto.Type fieldType = field.getType();
        final String scalarType = getJavaTypeName(fieldType);
        try {
            final Optional<? extends Class<?>> scalarPrimitive = getWrapperClass(scalarType);
            if (scalarPrimitive.isPresent()) {
                return ClassName.get(scalarPrimitive.get());
            }
            return ClassName.get(Class.forName(scalarType));
        } catch (ClassNotFoundException ex) {
            throw newIllegalArgumentException(
                    ex, "The class for the type: %s was not found.", field.getType()
            );
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
     * @param javaPackage the package of the class
     * @param typeCache   the cache of the message types
     * @param typeName    the name of the type
     * @return the constructed {@code ClassName}
     * @throws IllegalArgumentException if the class of the validating builder is not found
     */
    static ClassName getValidatorMessageClassName(String javaPackage,
                                                  MessageTypeCache typeCache,
                                                  String typeName) {
        checkNotNull(javaPackage);
        checkNotNull(typeCache);
        checkNotNull(typeName);

        final Collection<String> values = typeCache.getCachedTypes()
                                                   .values();
        final String expectedClassName = javaPackage + '.' + typeName;
        for (String value : values) {
            if (value.equals(expectedClassName)) {
                return ClassName.get(javaPackage, typeName);
            }
        }
        final String exMessage = format("The %s class is not found.", expectedClassName);
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
