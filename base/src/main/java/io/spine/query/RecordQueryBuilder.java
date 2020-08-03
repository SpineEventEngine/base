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
import io.spine.annotation.SPI;

/**
 * A builder for {@link RecordQuery}.
 *
 * @param <I>
 *         the type of identifiers of the queried records
 * @param <R>
 *         the type of the queried records
 */
@SPI
public class RecordQueryBuilder<I, R extends Message>
        extends AbstractQueryBuilder<I,
                                     R,
                                     RecordSubjectParameter<R, ?>,
                                     RecordQueryBuilder<I, R>,
                                     RecordQuery<I, R>> {

    protected RecordQueryBuilder(Class<I> idType, Class<R> recordType) {
        super(idType, recordType);
    }

    @Override
    protected RecordQueryBuilder<I, R> thisRef() {
        return this;
    }

    /**
     * Creates a new instance of {@link RecordQuery} basing on the data of this builder.
     *
     * <p>If the {@linkplain #limit(int) record limit} is set, checks that at least
     * one {@linkplain #addOrdering(OrderBy) ordering directive} is present.
     * Otherwise throws an {@linkplain IllegalStateException}.
     */
    @Override
    public RecordQuery<I, R> build() {
        return new RecordQuery<>(this);
    }

    /**
     * Creates a criterion for a particular record column.
     *
     * @param column
     *         the record column which will be queried
     * @param <V>
     *         the type of the record column values
     * @return a new criterion for the given column
     */
    public <V> RecordCriterion<I, R, V> where(RecordColumn<R, V> column) {
        return new RecordCriterion<>(column, this);
    }

    /**
     * Creates a criterion for the identifier values of the queried records.
     *
     * @return a new instance of a criterion
     */
    public IdCriterion<I, RecordQueryBuilder<I, R>> id() {
        return new IdCriterion<>(thisRef());
    }
}
