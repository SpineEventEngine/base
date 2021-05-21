/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.field;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.code.java.PrimitiveType;
import io.spine.code.proto.FieldDeclaration;

import java.util.List;
import java.util.Optional;

import static io.spine.tools.mc.java.field.StandardAccessor.add;
import static io.spine.tools.mc.java.field.StandardAccessor.addAll;
import static io.spine.tools.mc.java.field.StandardAccessor.clear;
import static io.spine.tools.mc.java.field.StandardAccessor.getCount;
import static io.spine.tools.mc.java.field.StandardAccessor.get;
import static io.spine.tools.mc.java.field.StandardAccessor.getList;
import static io.spine.tools.mc.java.field.StandardAccessor.set;

/**
 * Represents repeated {@linkplain FieldType field type}.
 */
final class RepeatedFieldType implements FieldType {

    private static final ImmutableSet<Accessor> GENERATED_ACCESSORS =
            ImmutableSet.of(
                    get(),
                    getList(),
                    getCount(),
                    set(),
                    add(),
                    addAll(),
                    clear()
            );

    private final TypeName typeName;

    /**
     * Constructs a new instance based on component type.
     *
     * @param declaration
     *         the declaration of the field
     */
    RepeatedFieldType(FieldDeclaration declaration) {
        this.typeName = constructTypeNameFor(declaration.javaTypeName());
    }

    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    @Override
    public ImmutableSet<Accessor> accessors() {
        return GENERATED_ACCESSORS;
    }

    /**
     * Returns "addAll" setter prefix, used to initialize a repeated field using with a call to
     * Protobuf message builder.
     */
    @Override
    public Accessor primarySetterTemplate() {
        return addAll();
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
