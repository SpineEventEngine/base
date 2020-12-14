/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.field.type;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.compiler.field.AccessorTemplate;

import java.util.AbstractMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.tools.compiler.field.AccessorTemplate.prefix;
import static io.spine.tools.compiler.field.AccessorTemplate.prefixAndPostfix;
import static io.spine.tools.compiler.field.AccessorTemplates.allPutter;
import static io.spine.tools.compiler.field.AccessorTemplates.clearer;
import static io.spine.tools.compiler.field.AccessorTemplates.countGetter;
import static io.spine.tools.compiler.field.AccessorTemplates.getter;
import static io.spine.tools.compiler.field.AccessorTemplates.mapGetter;
import static io.spine.tools.compiler.field.AccessorTemplates.putter;
import static io.spine.tools.compiler.field.AccessorTemplates.remover;

/**
 * Represents map {@linkplain FieldType field type}.
 */
public final class MapFieldType implements FieldType {

    private static final String GET = "get";

    private static final ImmutableSet<AccessorTemplate> GENERATED_ACCESSORS =
            ImmutableSet.of(
                    getter(),
                    countGetter(),
                    mapGetter(),
                    prefixAndPostfix(GET, "OrDefault"),
                    prefixAndPostfix(GET, "OrThrow"),
                    prefix("contains"),
                    clearer(),
                    putter(),
                    remover(),
                    allPutter()
            );

    private final TypeName typeName;
    /**
     * Constructs the new instance based on the key and the value type names.
     */
    MapFieldType(FieldDeclaration field) {
        Map.Entry<TypeName, TypeName> entryTypeNames = getEntryTypeNames(field);

        TypeName keyTypeName = boxIfPrimitive(entryTypeNames.getKey());
        TypeName valueTypeName = boxIfPrimitive(entryTypeNames.getValue());
        this.typeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                                                  keyTypeName,
                                                  valueTypeName);
    }

    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    /**
     * Returns "putAll" setter prefix,
     * used to initialize a map field using a protobuf message builder.
     *
     * <p>Call should be like `builder.putAllFieldName({@link Map})`.
     *
     * {@inheritDoc}
     */
    @Override
    public AccessorTemplate primarySetterTemplate() {
        return allPutter();
    }

    @Override
    public ImmutableSet<AccessorTemplate> generatedAccessorTemplates() {
        return GENERATED_ACCESSORS;
    }

    private static TypeName boxIfPrimitive(TypeName typeName) {
        if (typeName.isPrimitive()) {
            return typeName.box();
        }

        return typeName;
    }

    @Override
    public String toString() {
        return typeName.toString();
    }

    /**
     * Returns the key and the value type names for the map field
     * based on the passed nested types.
     */
    private static Map.Entry<TypeName, TypeName> getEntryTypeNames(FieldDeclaration mapField) {
        checkArgument(mapField.isMap());

        int keyFieldIndex = 0;
        int valueFieldIndex = 1;

        Descriptor mapEntry = mapField.descriptor()
                                      .getMessageType();

        FieldDescriptor keyField = mapEntry.getFields()
                                           .get(keyFieldIndex);
        FieldDescriptor valueField = mapEntry.getFields()
                                             .get(valueFieldIndex);

        TypeName keyTypeName = FieldType.of(new FieldDeclaration(keyField))
                                        .getTypeName();
        TypeName valueTypeName = FieldType.of(new FieldDeclaration(valueField))
                                          .getTypeName();

        return new AbstractMap.SimpleEntry<>(keyTypeName, valueTypeName);
    }
}
