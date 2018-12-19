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

package io.spine.validate;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A change in a {@linkplain io.spine.validate.FieldValue value of a field}.
 */
public final class FieldValueChange {

    /**
     * A value that used to be the value of the field.
     *
     * <p>Nullable because when a field is set for the first time, old value has never existed
     * and can be considered {@code null}.
     */
    private final @Nullable FieldValue oldValue;

    /**
     * A value that the field is going to obtain as a result of this change.
     */
    private final FieldValue newValue;

    /** Creates an instance of the field change from the given values. */
    private FieldValueChange(@Nullable FieldValue oldValue, FieldValue newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Creates a new instance of a field change based on the specified values.
     *
     * <p>{@code oldValue} can be {@code null}, because a first time the field is set the old
     * value does not exist, therefore can be considered {@code null}.
     *
     * @param oldValue
     *         a value that the field had before this change
     * @param newValue
     *         a value that the field is going to have as a result of this change
     * @return a new instance of a field change
     */
    public static FieldValueChange of(@Nullable FieldValue oldValue, FieldValue newValue) {
        return new FieldValueChange(oldValue, newValue);
    }

    /**
     * Creates a new instance of a field change that signifies the first change of the field.
     *
     * <p>If this method is used to instantiate a {@code FieldValueChange}, the old value is going
     * to be {@code null}.
     *
     * @param singleValue
     *         a value that the field is going to have as a result of this change
     * @return a new instance of a field change
     */
    static FieldValueChange firstValueEver(FieldValue singleValue) {
        return new FieldValueChange(null, singleValue);
    }

    /** Returns a value that the field is going to have as a result of this change. */
    FieldValue newValue() {
        return newValue;
    }

    /** Returns a value that the field used to have before this change took place.*/
    FieldValue oldValue() {
        return oldValue;
    }
}
