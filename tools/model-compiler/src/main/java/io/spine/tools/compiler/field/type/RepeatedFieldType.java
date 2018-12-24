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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.code.java.PrimitiveType;

import java.util.List;
import java.util.Optional;

/**
 * Represents repeated {@linkplain FieldType field type}.
 */
public final class RepeatedFieldType implements FieldType {

    private static final String SETTER_PREFIX = "addAll";

    private final TypeName typeName;

    /**
     * Constructs a new instance based on component type.
     *
     * @param componentTypeName the component type name
     */
    RepeatedFieldType(String componentTypeName) {
        this.typeName = constructTypeNameFor(componentTypeName);
    }

    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    /**
     * Returns "addAll" setter prefix,
     * used to initialize a repeated field using with a call to Protobuf message builder.
     */
    @Override
    public String getSetterPrefix() {
        return SETTER_PREFIX;
    }

    private static TypeName constructTypeNameFor(String componentTypeName) {
        Optional<? extends Class<?>> wrapperClass =
                PrimitiveType.getWrapperClass(componentTypeName);

        TypeName componentType = wrapperClass.isPresent()
                                 ? TypeName.get(wrapperClass.get())
                                 : ClassName.bestGuess(componentTypeName);
        ParameterizedTypeName result =
                ParameterizedTypeName.get(ClassName.get(List.class), componentType);
        return result;
    }

    @Override
    public String toString() {
        return typeName.toString();
    }
}
