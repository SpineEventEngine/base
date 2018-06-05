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
package io.spine.tools.compiler.fieldtype;

import com.google.common.base.Optional;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import io.spine.code.java.PrimitiveType;

/**
 * Represents singular {@linkplain FieldType field type}.
 *
 * @author Dmytro Grankin
 */
public class SingularFieldType implements FieldType {

    private static final String SETTER_PREFIX = "set";

    private final TypeName typeName;

    /**
     * Constructs the {@link SingularFieldType} based on field type name.
     *
     * @param name the field type name
     */
    SingularFieldType(String name) {
        this.typeName = constructTypeNameFor(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeName getTypeName() {
        return typeName;
    }

    /**
     * Returns "set" setter prefix,
     * used to initialize a singular field using a protobuf message builder.
     *
     * Call should be like `builder.setFieldName(FieldType)`.
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getSetterPrefix() {
        return SETTER_PREFIX;
    }

    private static TypeName constructTypeNameFor(String name) {
        final Optional<? extends Class<?>> boxedScalarPrimitive =
                PrimitiveType.getWrapperClass(name);

        return boxedScalarPrimitive.isPresent()
               ? TypeName.get(boxedScalarPrimitive.get())
                         .unbox()
               : ClassName.bestGuess(name);
    }

    @Override
    public String toString() {
        return typeName.toString();
    }
}
