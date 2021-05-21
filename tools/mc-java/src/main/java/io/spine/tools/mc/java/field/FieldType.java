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
import com.squareup.javapoet.TypeName;
import io.spine.code.proto.FieldDeclaration;

/**
 * The type information of a field for a code-generation.
 */
public interface FieldType {

    /**
     * Obtains the {@link TypeName} for the field.
     *
     * @return the type name
     */
    TypeName getTypeName();

    /**
     * Obtains the setter prefix for the field.
     *
     * @return the setter prefix
     */
    Accessor primarySetterTemplate();

    /**
     * Obtains the templates of the generated Java accessors for a field of this type.
     *
     * @return the accessor templates
     */
    ImmutableSet<Accessor> accessors();

    /**
     * Creates a an instances basing on the type of the field.
     */
    static FieldType of(FieldDeclaration field) {
        if (field.isMap()) {
            return new MapFieldType(field);
        } else if (field.isRepeated()) {
            return new RepeatedFieldType(field);
        } else {
            return new SingularFieldType(field);
        }
    }
}
