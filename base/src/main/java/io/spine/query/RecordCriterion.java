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

import com.google.protobuf.Message;

/**
 * //TODO:2020-06-23:alex.tymchenko: get rid of `I`.
 */
public final class RecordCriterion<I, R extends Message, V>
        extends QueryCriterion<R, V, RecordColumn<R, V>, RecordQueryBuilder<I, R>> {

    /**
     * Creates a new instance of {@code RecordCriterion}.
     *
     * @param column
     *         the column which actual value to use later in querying
     * @param builder
     *         the builder in scope of which this criterion exists
     */
    public RecordCriterion(RecordColumn<R, V> column, RecordQueryBuilder<I, R> builder) {
        super(column, builder);
    }

    @Override
    protected RecordQueryBuilder<I, R> addParameter(RecordQueryBuilder<I, R> builder,
                                                    RecordColumn<R, V> col, V value,
                                                    ComparisonOperator operator) {
        RecordSubjectParameter<R, V> parameter = new RecordSubjectParameter<>(col, value, operator);
        return builder.addParameter(parameter);
    }
}
