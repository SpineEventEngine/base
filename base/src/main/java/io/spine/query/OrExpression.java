/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import static io.spine.query.LogicalOperator.OR;

/**
 * Disjunctive expression.
 *
 * @param <R>
 *         the type of records, which parameters are a part of this expression
 */
final class OrExpression<R> extends Expression<R, OrExpression<R>> {

    private OrExpression(OrBuilder<R> builder) {
        super(OR, builder);
    }

    /**
     * Creates a new instance of builder for this expression.
     *
     * @param <R>
     *         the type of records, which parameters are a part of this expression
     */
    static <R> OrBuilder<R> newBuilder() {
        return new OrBuilder<>();
    }

    @Override
    Builder<R, OrExpression<R>, ?> createBuilder() {
        return newBuilder();
    }

    /**
     * Treats the passed expression as {@code OrExpression} and transforms it to its builder.
     */
    static <R> OrBuilder<R> asOrBuilder(Expression<R, ?> expression) {
        @SuppressWarnings("unchecked")
        OrBuilder<R> resultBuilder = (OrBuilder<R>)
                asOr(expression)
                        .toBuilder();
        return resultBuilder;
    }

    /**
     * Attempts to cast the passed expression to {@code OrExpression}.
     */
    @SuppressWarnings("unchecked")
    static <R> OrExpression<R> asOr(Expression<R, ?> expression) {
        return (OrExpression<R>) expression;
    }

    /**
     * Builder of {@code OrExpression}.
     *
     * @param <R>
     *         the type of records, around which the expression is built
     */
    static final class OrBuilder<R> extends Builder<R, OrExpression<R>, OrBuilder<R>> {

        @Override
        OrBuilder<R> thisRef() {
            return this;
        }

        @Override
        OrExpression<R> build() {
            return new OrExpression<>(this);
        }
    }
}
