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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Joins the {@linkplain SubjectParameter subject parameters} with
 * {@linkplain LogicalOperator logical operators}.
 *
 * @param <P>
 *         the type of query parameters used in the predicate
 */
public final class Predicate<P extends SubjectParameter<?, ?>> {

    /**
     * Defines whether the parameters are evaluated in conjunction or disjunction with each other.
     */
    private final LogicalOperator operator;

    /**
     * The list of parameters, each joined with the rest of parameters (including the custom params)
     * with the same logical operator.
     */
    private final ImmutableList<P> parameters;

    /**
     * The list of parameters, each joined with the rest of parameters
     * with the same logical operator.
     */
    private final ImmutableList<CustomSubjectParameter<?, ?>> customParameters;

    /**
     * Creates a new {@code Predicate}.
     */
    private Predicate(Builder<P> builder) {
        this.operator = builder.operator;
        this.parameters = ImmutableList.copyOf(builder.parameters);
        this.customParameters = ImmutableList.copyOf(builder.customParameters);
    }

    /**
     * Creates a new instance of {@code Predicate.Builder} for a specified logical operator.
     *
     * @param <P>
     *         the type of query parameters used in the predicate
     */
    static <P extends SubjectParameter<?, ?>> Builder<P> newBuilder(LogicalOperator operator) {
        return new Builder<>(operator);
    }

    /**
     * Returns an operator with which the parameters are joined.
     */
    public LogicalOperator operator() {
        return operator;
    }

    /**
     * Returns the list of parameters of this {@code Predicate}.
     */
    public ImmutableList<P> parameters() {
        return parameters;
    }

    /**
     * Returns the list of custom parameters of this {@code Predicate}.
     */
    public ImmutableList<CustomSubjectParameter<?, ?>> customParameters() {
        return customParameters;
    }

    /**
     * Builds {@link Predicate} instances.
     */
    static final class Builder<P extends SubjectParameter<?, ?>> {

        private final LogicalOperator operator;
        private final List<P> parameters = new ArrayList<>();
        private final List<CustomSubjectParameter<?, ?>> customParameters = new ArrayList<>();

        /**
         * Does not allow to instantiate this class directly.
         *
         * @param operator
         *         the operator which defines whether the parameters are evaluated
         *         in conjunction or disjunction with each other
         * @see Predicate#newBuilder(LogicalOperator)
         */
        private Builder(LogicalOperator operator) {
            this.operator = operator;
        }

        /**
         * Adds a parameter to the predicate.
         */
        Builder<P> add(P parameter) {
            checkNotNull(parameter);
            this.parameters.add(parameter);
            return this;
        }

        /**
         * Adds a parameter, which targets some custom or computed property of the record.
         */
        Builder<P> addCustom(CustomSubjectParameter<?, ?> parameter) {
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
         * Builds a new instance of a {@code Predicate} based on the data in this {@code Builder}.
         */
        Predicate<P> build() {
            checkState(hasParams(),
                       "Query predicate must have at least one subject parameter.");
            return new Predicate<>(this);
        }
    }
}
