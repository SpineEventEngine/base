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

package io.spine.tools.compiler.field.type;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import io.spine.code.java.PrimitiveType;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.compiler.field.AccessorTemplate;

import java.util.Optional;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.STRING;
import static io.spine.tools.compiler.field.AccessorTemplate.prefix;
import static io.spine.tools.compiler.field.AccessorTemplate.prefixAndPostfix;
import static io.spine.tools.compiler.field.AccessorTemplates.clearer;
import static io.spine.tools.compiler.field.AccessorTemplates.getter;
import static io.spine.tools.compiler.field.AccessorTemplates.setter;

/**
 * Represents singular {@linkplain FieldType field type}.
 */
public final class SingularFieldType implements FieldType {

    private static final String BYTES = "Bytes";

    private static final ImmutableSet<AccessorTemplate> GENERATED_ACCESSORS = ImmutableSet.of(
            prefix("has"),
            getter(),
            setter(),
            clearer()
    );

    private static final ImmutableSet<AccessorTemplate> GENERATED_STRING_ACCESSORS =
            ImmutableSet.of(
                    prefixAndPostfix("get", BYTES),
                    prefixAndPostfix("set", BYTES)
            );

    private final TypeName typeName;
    private final JavaType javaType;

    /**
     * Creates a new instance based on field type name.
     *
     * @param declaration
     *         the field declaration
     */
    SingularFieldType(FieldDeclaration declaration) {
        this.typeName = constructTypeNameFor(declaration.javaTypeName());
        this.javaType = declaration.javaType();
    }

    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    @Override
    public ImmutableSet<AccessorTemplate> generatedAccessorTemplates() {
        return javaType == STRING
             ? ImmutableSet.<AccessorTemplate>builder()
                       .addAll(GENERATED_ACCESSORS)
                       .addAll(GENERATED_STRING_ACCESSORS)
                       .build()
             : GENERATED_ACCESSORS;
    }

    /**
     * Returns "set" setter template used to initialize a singular field using a Protobuf message
     * builder.
     *
     * <p>The call should have the following structure: {@code builder.setFieldName(FieldType)}.
     */
    @Override
    public AccessorTemplate primarySetterTemplate() {
        return setter();
    }

    private static TypeName constructTypeNameFor(String name) {
        Optional<? extends Class<?>> boxedScalarPrimitive =
                PrimitiveType.getWrapperClass(name);

        if (boxedScalarPrimitive.isPresent()) {
            TypeName unboxed = TypeName.get(boxedScalarPrimitive.get())
                                       .unbox();
            return unboxed;
        }

        // Make a possibly nested class name use the dot notation.
        String dottedName = name.replace('$', '.');
        ClassName result = ClassName.bestGuess(dottedName);
        return result;
    }
}
