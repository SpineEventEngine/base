/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.query;

/**
 * A subject parameter for a query, which defines a condition for something different than
 * a value of a field declared in a {@code Message} type of the record.
 *
 * <p>In some cases, stored records or entities include {@linkplain CustomColumn custom columns},
 * which hold the properties not directly belonging to the record declaration. In order to include
 * such a column into a {@link Subject} definition, a {@code CustomQueryParameter} is used.
 *
 * @param <S>
 *         the type of objects serving as an origin for the parameter values
 * @param <V>
 *         the type of parameter values
 * @see CustomColumn
 * @see Subject
 */
public final class CustomSubjectParameter<S, V> extends SubjectParameter<S, Column<S, V>, V> {

    /**
     * Creates a new instance.
     *
     * @param column
     *         column to use in the query
     * @param value
     *         the value against which the actual column values will be compared when querying
     * @param operator
     *         the comparison operator
     */
    CustomSubjectParameter(CustomColumn<S, V> column, V value, ComparisonOperator operator) {
        super(column, operator, value);
    }
}
