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
import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.Internal;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A number that is described with a {@code String} of characters.
 */
@Immutable
@Internal
public final class NumberText {

    private static final ImmutableList<Class<? extends Number>> SUPPORTED_NUMBERS =
            ImmutableList.of(Integer.class, Long.class, Double.class, Float.class);
    private static final String DECIMAL_DELIMITER = ".";
    private static final Splitter DECIMAL_SPLIT = Splitter.on(DECIMAL_DELIMITER);

    private final String text;
    @SuppressWarnings("Immutable") // effectively
    private final Number value;

    /** Creates a new instance that is equal to the specified number. */
    public NumberText(Number number) {
        this.text = String.valueOf(number);
        this.value = checkSupportedNumber(number);
    }

    /**
     * Creates a new instance, the value being the specified {@code String} resolved to an
     * appropriate type.
     *
     * @param text
     *         a string representation of a number
     */
    public NumberText(String text) {
        this.text = checkNotEmptyOrBlank(text).trim();
        this.value = parseNumber(this.text);
    }

    /**
     * Determines whether this instance of a number represents the same number type as the
     * supplied one.
     *
     * <p>Example:
     * <pre>
     *     {@code
     *      NumberText zeroWithDecimal = new NumberText("0.0");
     *      NumberText plainZero = new NumberText("0");
     *
     *      zeroWithDecimal.isOfSameType(plainZero); // false
     *      }
     *  </pre>
     * the above code is false, since {@code zeroWithDecimal} describes a number that has a decimal
     * part, be it a {@code Double} or a {@code Float}, while {@code plainZero} describes an
     * arbitrary whole number.
     *
     * @return {@code true} if the instance of a number represents the same number type as the
     *         supplied one, {@code false} otherwise
     */
    public boolean isOfSameType(NumberText anotherNumber) {
        return areWholeNumbers(this, anotherNumber) || areFractionalNumbers(this, anotherNumber);
    }

    /**
     * Converts current number {@code value} to a {@code ComparableNumber}.
     */
    public ComparableNumber toNumber() {
        return new ComparableNumber(this.value);
    }

    private static Number checkSupportedNumber(Number value) {
        Class<? extends Number> numberClass = value.getClass();
        checkArgument(SUPPORTED_NUMBERS.contains(numberClass),
                      "NumberText can only represent one of supported types: %s, but was %s",
                      SUPPORTED_NUMBERS, numberClass);
        return value;
    }

    private static boolean areFractionalNumbers(NumberText first, NumberText second) {
        return !isWholeNumber(first) && !isWholeNumber(second);
    }

    private static boolean areWholeNumbers(NumberText first, NumberText second) {
        return isWholeNumber(first) && isWholeNumber(second);
    }

    /**
     * Determines if the number text represents a whole number.
     *
     * <p>While Protobuf supports only {@code int} and {@code long} values in Java, we assume
     * that only these values denotes whole numbers.
     */
    private static boolean isWholeNumber(NumberText numberText) {
        Class<? extends Number> classOfNumber = numberText.value.getClass();
        return Integer.class.equals(classOfNumber) || Long.class.equals(classOfNumber);
    }

    private static Number parseNumber(String text) {
        List<String> wholeAndDecimal = DECIMAL_SPLIT.splitToList(text);
        hasOnlyWholeAndDecimal(wholeAndDecimal);
        if (hasDecimalPart(wholeAndDecimal)) {
            return Double.parseDouble(text);
        }
        return Long.parseLong(text);
    }

    private static boolean hasDecimalPart(List<String> wholeAndDecimal) {
        boolean hasOnlyWhole = wholeAndDecimal.size() <= 1;
        return !hasOnlyWhole && !wholeAndDecimal.get(1)
                                                .isEmpty();
    }

    private static void hasOnlyWholeAndDecimal(Collection<String> wholeAndDecimal)
            throws IllegalStateException {
        if (wholeAndDecimal.size() > 2) {
            String malformedNumber = String.join("", wholeAndDecimal);
            throw newIllegalStateException("Found malformed number: %s.", malformedNumber);
        }
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
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
        return Objects.equal(this.value, text.value);
    }
}
