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

package io.spine.validate;

import com.google.common.collect.ImmutableList;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A constraint that is applicable to numeric fields only.
 *
 * @param <V>
 *         a type of values that this constraint is applicable to.
 */
@SuppressWarnings("TypeParameterUnusedInFormals")
// Numbers are not comparable, so having a V that is both a Number and Comparable is necessary.
public abstract class NumericFieldConstraint<V extends Number & Comparable, T>
        extends FieldValueConstraint<V, T> {

    NumericFieldConstraint(T optionValue) {
        super(optionValue);
    }

    @Override
    public ImmutableList<ConstraintViolation> check(FieldValue<V> value) {
        if (!satisfies(value)) {
            return constraintViolated(value);
        }
        return ImmutableList.of();
    }

    /** Returns a number of type V based on its string representation. */
    static <V extends Number & Comparable> V fromStringValue(String numericValue) {
        try {
            V result = fromString(numericValue);
            return result;
        } catch (ParseException e) {
            String errorMessage = "Could not extract numeric value from an option. Value: %s";
            throw newIllegalStateException(e, errorMessage, numericValue);
        }
    }

    @SuppressWarnings("unchecked") // Safe since the returned value is always both a Number and Comparable.
    private static <V extends Number & Comparable> V fromString(String input)
            throws ParseException {
        NumberFormat numberFormat = new DecimalFormat();
        Number number = numberFormat.parse(input);
        if (fitsIntoInt(number) && whole(number)) {
            @SuppressWarnings("WrapperTypeMayBePrimitive") // Primitives are uncastable.
            Integer result = number.intValue();
            return (V) result;
        }
        return (V) number;
    }

    private static boolean fitsIntoInt(Number number) {
        return number.longValue() <= Integer.MAX_VALUE;
    }

    private static boolean whole(Number number) {
        return number.doubleValue() % 1 == 0;
    }

    /**
     * Whether the actual value of the field satisfies this constraint.
     *
     * @param value
     *         a value of the field.
     * @return {@code true} the specified does not satisfy this constraint
     */
    abstract boolean satisfies(FieldValue<V> value);

    /** Violations that should be produced if this constraint is not satisfied. */
    abstract ImmutableList<ConstraintViolation> constraintViolated(FieldValue<V> value);
}
