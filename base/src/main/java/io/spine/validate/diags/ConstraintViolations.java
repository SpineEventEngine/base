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

package io.spine.validate.diags;

import io.spine.validate.ConstraintViolation;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;

/**
 * Utility class for working with {@link io.spine.validate.ConstraintViolation}s.
 */
public final class ConstraintViolations {

    /** Prevent instantiation of this utility class. */
    private ConstraintViolations() {
    }

    /**
     * Returns a formatted string using the format string and parameters from the violation.
     *
     * @param violation violation which contains the format string and
     *                  arguments referenced by the format specifiers in it
     * @return a formatted string
     * @see String#format(String, Object...)
     */
    public static String toText(ConstraintViolation violation) {
        checkNotNull(violation);

        String format = violation.getMsgFormat();
        List<String> params = violation.getParamList();
        String parentViolationFormatted = format(format, params.toArray());

        StringBuilder resultBuilder = new StringBuilder(parentViolationFormatted);
        if (violation.getViolationCount() > 0) {
            resultBuilder.append(toText(violation.getViolationList()));
        }
        return resultBuilder.toString();
    }

    /**
     * Returns a formatted string using the format string and parameters from each of
     * the violations passed.
     *
     * @param violations violations which contain the format string and
     *                   arguments referenced by the format specifiers in each of them
     * @return a formatted string
     * @see #toText(ConstraintViolation)
     */
    public static String toText(Iterable<ConstraintViolation> violations) {
        checkNotNull(violations);

        StringBuilder resultBuilder = new StringBuilder("Violations:");

        String newLine = lineSeparator();
        for (ConstraintViolation childViolation : violations) {
            String childViolationFormatted = toText(childViolation);
            resultBuilder.append(newLine)
                         .append(childViolationFormatted);
        }
        return resultBuilder.toString();
    }

    /**
     * Returns a formatted string using the specified format string and parameters
     * from the violation.
     *
     * @param format    a format string
     * @param violation violation which contains arguments referenced by the format
     *                  specifiers in the format string
     * @return a formatted string
     * @see String#format(String, Object...)
     */
    public static String toText(String format, ConstraintViolation violation) {
        checkNotNull(format);
        checkNotNull(violation);

        List<String> params = violation.getParamList();
        String parentViolationFormatted = format(format, params.toArray());

        StringBuilder resultBuilder = new StringBuilder(parentViolationFormatted);
        if (violation.getViolationCount() > 0) {
            resultBuilder.append(toText(format, violation.getViolationList()));
        }

        return resultBuilder.toString();
    }

    /**
     * Returns a formatted string using the specified format string and parameters from
     * each of the violations passed.
     *
     * @param format     a format string
     * @param violations violations which contain the arguments referenced by the format
     *                   specifiers in the format string
     * @return a formatted string
     * @see String#format(String, Object...)
     */
    public static String toText(String format, Iterable<ConstraintViolation> violations) {
        checkNotNull(format);
        checkNotNull(violations);

        StringBuilder resultBuilder = new StringBuilder("Violations:");

        String newLine = lineSeparator();
        for (ConstraintViolation childViolation : violations) {
            String childViolationFormatted = toText(format, childViolation);
            resultBuilder.append(newLine)
                         .append(childViolationFormatted);
        }
        return resultBuilder.toString();
    }

}
