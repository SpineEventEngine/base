/*
 * Copyright 2022, TeamDev. All rights reserved.
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static java.util.Objects.requireNonNull;

/**
 * Transforms the {@link QueryPredicate} into its disjunctive normal form.
 *
 * <p>If the predicate has no {@code AND -> OR} in parent-child nesting, returns it as-is.
 *
 * <p>Examples.
 *
 * <ul>
 *      <li>{@code A && B && (C || D)} becomes {@code (A && B && C) || (A && B && D)}
 *
 *      <li>{@code (A && B) || (D && (C || E || (F && G)))} becomes
 *          {@code (A && B) || (D && C) || (D && E) || (D && F && G)}
 *
 *      <li>{@code A && B && C} stays the same.
 * </ul>
 *
 * @param <R>
 *         the type of the records targeted by the predicate
 * @see <a href="https://en.wikipedia.org/wiki/Disjunctive_normal_form">Disjunctive normal
 *         form</a>
 */
final class TransformToDnf<R> implements UnaryOperator<QueryPredicate<R>> {

    @Override
    public QueryPredicate<R> apply(QueryPredicate<R> source) {
        var expression = asExpression(source);
        var flat = flatten(expression);
        var result = fromExpression(flat);
        return result;
    }

    /**
     * Transforms the predicate to an {@code Expression}.
     */
    private static <R> Expression<R, ?> asExpression(QueryPredicate<R> predicate) {
        Expression.Builder<R, ?, ?> builder;
        if (predicate.operator() == AND) {
            builder = AndExpression.newBuilder();
        } else {
            builder = OrExpression.newBuilder();
        }
        builder.addParams(predicate.parameters())
               .addCustomParams(predicate.customParameters());

        for (var child : predicate.children()) {
            var expression = asExpression(child);
            builder.addExpression(expression);
        }
        var result = builder.build();
        return result;
    }

    /**
     * Transforms the {@code Expression} back to the {@code Predicate}.
     */
    private static <R> QueryPredicate<R> fromExpression(Expression<R, ?> expression) {
        QueryPredicate.Builder<R> builder = QueryPredicate.newBuilder(expression.operator());
        builder.addParams(expression.params())
               .addCustomParams(expression.customParams());
        for (var childExpression : expression.children()) {
            var childPredicate = fromExpression(childExpression);
            builder.addPredicate(childPredicate);
        }
        var result = builder.build();
        return result;
    }

    /**
     * Attempts to flatten the expression tree and reduce its depth by applying the distributive law
     * to the grandchild nodes.
     */
    private static <R> Expression<R, ?> flatten(Expression<R, ?> expression) {
        List<Expression<R, ?>> children = expression.children();
        if (children.isEmpty()) {
            return expression;
        }
        Expression<R, ?> flattened;
        if (expression.operator() == AND) {
            flattened = handleAnd(expression);
        } else {
            flattened = handleOr(expression);
        }
        return flattened;
    }

    private static <R> Expression<R, ?> handleOr(Expression<R, ?> expression) {
        Expression<R, ?> flattened;
        var flatExpressions = flatten(expression.children());
        var resultBuilder = OrExpression.asOrBuilder(expression)
                                        .clearChildren();
        for (var flatExpression : flatExpressions) {
            if (flatExpression.operator() == OR) {
                OrExpression.asOr(flatExpression)
                            .copyTo(resultBuilder);
            } else {
                resultBuilder.addExpression(flatExpression);
            }
        }
        flattened = resultBuilder.build();
        return flattened;
    }

    private static <R> Expression<R, ?> handleAnd(Expression<R, ?> expression) {
        Expression<R, ?> flattened;
        Queue<Expression<R, ?>> flatExpressions = flatten(expression.children());
        var paramExpression = expression.withoutChildren();
        if (!paramExpression.isEmpty()) {
            flatExpressions.add(paramExpression);
        }

        var head = flatExpressions.poll();
        requireNonNull(head, "Head of the expression tree cannot be `null`.");
        while (!flatExpressions.isEmpty()) {
            var next = flatExpressions.poll();
            var distributed = Distribution.conjunctive(head, next);
            head = flatten(distributed);
        }
        flattened = head;
        return flattened;
    }

    private static <R> Deque<Expression<R, ?>> flatten(List<Expression<R, ?>> children) {
        Deque<Expression<R, ?>> flatExpressions = new ArrayDeque<>();
        for (var child : children) {
            if (child.hasChildren()) {
                var flattenedChild = flatten(child);
                flatExpressions.add(flattenedChild);
            } else {
                flatExpressions.add(child);
            }
        }
        return flatExpressions;
    }
}
