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

package io.spine.tools.compiler.fieldtype;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Map;

/**
 * Represents map {@linkplain FieldType field type}.
 */
public class MapFieldType implements FieldType {

    private static final String SETTER_PREFIX = "putAll";

    private final TypeName typeName;
    private final TypeName keyTypeName;
    private final TypeName valueTypeName;

    /**
     * Constructs the {@link MapFieldType} based on
     * the key and the value type names.
     *
     * @param entryTypeNames the entry containing the key and the value type names.
     */
    MapFieldType(Map.Entry<TypeName, TypeName> entryTypeNames) {
        this.keyTypeName = boxIfPrimitive(entryTypeNames.getKey());
        this.valueTypeName = boxIfPrimitive(entryTypeNames.getValue());
        this.typeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                                                  keyTypeName,
                                                  valueTypeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    public TypeName getKeyTypeName() {
        return keyTypeName;
    }

    public TypeName getValueTypeName() {
        return valueTypeName;
    }

    /**
     * Returns "putAll" setter prefix,
     * used to initialize a map field using a protobuf message builder.
     *
     * <p>Call should be like `builder.putAllFieldName({@link Map})`.
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getSetterPrefix() {
        return SETTER_PREFIX;
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
}
