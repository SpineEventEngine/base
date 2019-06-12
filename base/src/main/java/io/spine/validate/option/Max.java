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
import io.spine.option.MaxOption;
import io.spine.option.OptionsProto;
import io.spine.validate.FieldValue;

/**
 * An option that defines a maximum value for a numeric field.
 *
 * @param <V>
 *         numeric value type that this option is applied to
 */
@Immutable
final class Max<@ImmutableTypeParameter V extends Number & Comparable<V>>
        extends FieldValidatingOption<MaxOption, V> {

    private Max() {
        super(OptionsProto.max);
    }

    /** Returns a new instance of this option. */
    static <@ImmutableTypeParameter V extends Number & Comparable<V>> Max<V> create() {
        return new Max<>();
    }

    @Override
    public Constraint<FieldValue<V>> constraintFor(FieldValue<V> fieldValue) {
        return new MaxConstraint<>(optionValue(fieldValue));
    }
}
