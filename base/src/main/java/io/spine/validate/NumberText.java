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

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A number that is described with a {@code String} of characters.
 */
final class NumberText {

    private static final String DECIMAL_DELIMITER = ".";
    private static final Splitter DECIMAL_SPLIT = Splitter.on(DECIMAL_DELIMITER);

    private final String stringRepresentation;
    private final Number numericValue;

    /** Creates a new instance that is equal to the specified number. */
    NumberText(Number number) {
        this.stringRepresentation = String.valueOf(number);
        this.numericValue = number;
    }

    /**
     * Creates a new instance, the value being the specified {@code String} resolved to an
     * appropriate type.
     *
     * @param representation
     *         a string representation of a number
     */
    NumberText(String representation) {
        this.stringRepresentation = checkNotEmptyOrBlank(representation).trim();
        this.numericValue = parseNumber(stringRepresentation);
    }

    /**
     * Returns whether this instance of a number is of the same {@code Number} subtype as
     * the specified one.
     *
     * <p>Example:
     * <pre>
     * {@code
     *   NumberText zeroWithDecimal = new NumberText("0.0");
     *   NumberText plainZero = new NumberText("0");
     *
     *   zeroWithDecimal.isOfSameType(plainZero); // false
     * }
     *  </pre>
     * the above code is false, since {@code zeroWithDecimal} describes a number that has a decimal
     * part, be it a {@code Double} or a {@code Float}, while {@code plainZero} describes an
     * arbitrary whole number.
     *
     * @return whether this instance of a number is of the same {@code Number} subtype as
     *         the specified one.
     */
    boolean isOfSameType(NumberText anotherNumber) {
        Class<? extends Number> classOfThisNumber = numericValue.getClass();
        Class<? extends Number> classOfAnotherNumber = anotherNumber.numericValue.getClass();
        return classOfThisNumber.equals(classOfAnotherNumber);
    }

    private static Number parseNumber(String stringRepresentation) {
        ImmutableList<String> wholeAndDecimal =
                ImmutableList.copyOf(DECIMAL_SPLIT.split(stringRepresentation));
        hasOnlyWholeAndDecimal(wholeAndDecimal);
        if (hasDecimalPart(stringRepresentation)) {
            return Double.parseDouble(stringRepresentation);
        }
        if (fitsIntoInteger(stringRepresentation)) {
            return Integer.parseInt(stringRepresentation);
        } else {
            return Long.parseLong(stringRepresentation);
        }
    }

    private static boolean hasDecimalPart(String stringRepresentation) {
        ImmutableList<String> wholeAndDecimal =
                ImmutableList.copyOf(DECIMAL_SPLIT.split(stringRepresentation));
        boolean hasOnlyWhole = wholeAndDecimal.size() <= 1;
        return !hasOnlyWhole && !wholeAndDecimal.get(1)
                                                .isEmpty();
    }

    private static boolean fitsIntoInteger(String representation) {
        long number = Long.parseLong(representation);
        return Integer.MAX_VALUE >= number;
    }

    private static void hasOnlyWholeAndDecimal(Collection<String> wholeAndDecimal)
            throws IllegalStateException {
        if (wholeAndDecimal.size() > 2) {
            String malformedNumber = String.join("", wholeAndDecimal);
            throw newIllegalStateException("Found malformed number: %s.", malformedNumber);
        }
    }

    ComparableNumber toNumber() {
        return new ComparableNumber(this.numericValue);
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numericValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NumberText text = (NumberText) o;
        return Objects.equal(stringRepresentation, text.stringRepresentation) &&
                Objects.equal(numericValue, text.numericValue);
    }
}
