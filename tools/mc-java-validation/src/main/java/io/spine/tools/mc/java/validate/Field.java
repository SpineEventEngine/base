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

package io.spine.tools.mc.java.validate;

import com.google.common.base.Objects;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A field to be attached to a Java class.
 *
 * @implNote A {@code Field} wraps a JavaPoet {@link FieldSpec} which can be added to a JavaPoet
 *         {@link TypeSpec} builder.
 */
final class Field implements ClassMember {

    private final FieldSpec fieldSpec;

    Field(FieldSpec fieldSpec) {
        this.fieldSpec = checkNotNull(fieldSpec);
    }

    @Override
    public void attachTo(TypeSpec.Builder type) {
        type.addField(fieldSpec);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Field)) {
            return false;
        }
        Field field = (Field) o;
        return Objects.equal(fieldSpec, field.fieldSpec);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldSpec);
    }
}
