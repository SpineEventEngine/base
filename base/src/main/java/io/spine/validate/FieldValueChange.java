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


/**
 * A change in a {@linkplain io.spine.validate.FieldValue value of a field}.
 */
final class FieldValueChange {

    /** A value that used to be the value of the field. */
    private final FieldValue previousValue;

    /**
     * A value that the field is going to obtain as a result of this change.
     */
    private final FieldValue newValue;

    /** Creates an instance of the field change from the given values. */
    private FieldValueChange(FieldValue previousValue, FieldValue newValue) {
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    /**
     * Creates a new instance of a field change based on the specified values.
     *
     * @param oldValue
     *         a value that the field had before this change
     * @param newValue
     *         a value that the field is going to have as a result of this change
     * @return a new instance of a field change
     */
    public static FieldValueChange of(FieldValue oldValue, FieldValue newValue) {
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
    static FieldValueChange withoutPreviousValue(FieldValue singleValue) {
        FieldContext context = singleValue.context();
        FieldValue unset = FieldValue.unsetValue(context);
        return new FieldValueChange(unset, singleValue);
    }

    /** Returns a value that the field is going to have as a result of this change. */
    FieldValue newValue() {
        return newValue;
    }

    /**
     * Returns a value that the field used to have before this change took place.
     *
     * <p>May be {@linkplain io.spine.validate.FieldValue#unsetValue(FieldContext) unset}.
     */
    FieldValue previousValue() {
        return previousValue;
    }

    boolean isFirstTimeSet(){
        return !previousValue.isSet();
    }
}
