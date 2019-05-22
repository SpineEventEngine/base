/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import io.spine.validate.FieldValue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A rule that limits a set of values that a Protobuf field can have.
 *
 * @param <T>
 *         a type of value of the field that this constraint is applied to
 * @param <V>
 *         a type of value that describes the constraints
 */
@Immutable
public abstract class FieldValueConstraint<@ImmutableTypeParameter T, @ImmutableTypeParameter V>
        implements Constraint<FieldValue<T>> {

    private final V optionValue;

    /**
     * Creates a new instance of this constraint.
     *
     * @param optionValue
     *         a value that describes the field constraints
     */
    protected FieldValueConstraint(V optionValue) {
        checkNotNull(optionValue);
        this.optionValue = optionValue;
    }

    /** Returns a value that describes the constraint.*/
    public V optionValue() {
        return optionValue;
    }
}
