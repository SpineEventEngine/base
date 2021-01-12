/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.query;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Joins the {@linkplain SubjectParameter query parameters} with
 * a {@linkplain LogicalOperator logical operator} when querying the records.
 *
 * @param <R>
 *         the type of the queried records
 */
public final class QueryPredicate<R> {

    /**
     * Defines whether the parameters are evaluated in conjunction or disjunction with each other.
     */
    private final LogicalOperator operator;

    /**
     * The list of query parameters for the own columns declared by the queried record.
     */
    private final ImmutableList<SubjectParameter<R, ?, ?>> parameters;

    /**
     * The list of query parameters which address the custom columns of the queried record.
     */
    private final ImmutableList<CustomSubjectParameter<?, ?>> customParameters;

    /**
     * Creates a new {@code Predicate}.
     */
    private QueryPredicate(Builder<R> builder) {
        this.operator = checkNotNull(builder.operator);
        checkNotNull(builder.parameters);
        this.parameters = ImmutableList.copyOf(builder.parameters);
        checkNotNull(builder.customParameters);
        this.customParameters = ImmutableList.copyOf(builder.customParameters);
    }

    /**
     * Creates a new instance the predicate builder for a specified logical operator.
     *
     * @param <R>
     *         the type of the record which is stored for subject
     */
    static <R> Builder<R> newBuilder(LogicalOperator operator) {
        return new Builder<>(operator);
    }

    /**
     * Returns an operator with which the parameters are joined.
     */
    public LogicalOperator operator() {
        return operator;
    }

    /**
     * Returns the parameters of this predicate which query the values of own columns declared
     * in the queried record.
     */
    public ImmutableList<SubjectParameter<R, ?, ?>> parameters() {
        return parameters;
    }

    /**
     * Returns the parameters of this predicate which relate to the custom columns
     * of the queried record.
     */
    public ImmutableList<CustomSubjectParameter<?, ?>> customParameters() {
        return customParameters;
    }

    /**
     * Returns the list of all parameters of this predicate, including both
     * {@linkplain #parameters() parameters for the own record columns}
     * and those {@linkplain #customParameters() defined for the custom columns}
     * of the queried record.
     */
    public ImmutableList<SubjectParameter<?, ?, ?>> allParams() {
        return ImmutableList.<SubjectParameter<?, ?, ?>>builder()
                .addAll(parameters())
                .addAll(customParameters())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QueryPredicate)) {
            return false;
        }
        QueryPredicate<?> predicate = (QueryPredicate<?>) o;
        return operator == predicate.operator &&
                parameters.equals(predicate.parameters) &&
                customParameters.equals(predicate.customParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, parameters, customParameters);
    }

    /**
     * Builds {@link QueryPredicate} instances.
     *
     * @param <R>
     *         the type of the queried record
     */
    static final class Builder<R> {

        private final LogicalOperator operator;
        private final List<SubjectParameter<R, ?, ?>> parameters = new ArrayList<>();
        private final List<CustomSubjectParameter<?, ?>> customParameters = new ArrayList<>();

        /**
         * Does not allow to instantiate this class directly.
         *
         * @param operator
         *         the operator which defines whether the parameters are evaluated
         *         in conjunction or disjunction with each other
         * @see QueryPredicate#newBuilder(LogicalOperator)
         */
        private Builder(LogicalOperator operator) {
            this.operator = operator;
        }

        /**
         * Adds a parameter for the own column declared by the queried records.
         */
        @CanIgnoreReturnValue
        Builder<R> add(SubjectParameter<R, ?, ?> parameter) {
            checkNotNull(parameter);
            this.parameters.add(parameter);
            return this;
        }

        /**
         * Adds a parameter, which addresses the {@linkplain CustomColumn custom column}
         * of the queried records.
         */
        @CanIgnoreReturnValue
        Builder<R> addCustom(CustomSubjectParameter<?, ?> parameter) {
            checkNotNull(parameter);
            customParameters.add(parameter);
            return this;
        }

        /**
         * Tells if there is at least one parameter added.
         */
        boolean hasParams() {
            return !parameters.isEmpty() || !customParameters.isEmpty();
        }

        /**
         * Builds a new instance of a predicate based on the data in this builder.
         */
        QueryPredicate<R> build() {
            return new QueryPredicate<>(this);
        }
    }
}
