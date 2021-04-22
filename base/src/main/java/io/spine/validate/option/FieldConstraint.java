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

package io.spine.validate.option;

import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.ImmutableTypeParameter;
import io.spine.code.proto.FieldDeclaration;
import io.spine.type.MessageType;
import io.spine.validate.Constraint;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A rule that limits a set of values that a Protobuf field can have.
 *
 * @param <V>
 *         a type of value that describes the constraints
 */
@Immutable
public abstract class FieldConstraint<@ImmutableTypeParameter V> implements Constraint {

    private final V optionValue;
    private final FieldDeclaration field;

    /**
     * Creates a new instance of this constraint.
     *
     * @param optionValue
     *         a value that describes the field constraints
     * @param field
     *         the field which declares the constraint
     */
    protected FieldConstraint(V optionValue, FieldDeclaration field) {
        this.optionValue = checkNotNull(optionValue);
        this.field = checkNotNull(field);
    }

    /** Returns a value that describes the constraint.*/
    public final V optionValue() {
        return optionValue;
    }

    public final FieldDeclaration field() {
        return field;
    }

    @Override
    public MessageType targetType() {
        return field.declaringType();
    }
}
