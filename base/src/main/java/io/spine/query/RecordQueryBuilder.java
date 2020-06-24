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
                                     RecordQueryParameter<R, ?>,
                                     RecordQueryBuilder<I, R>,
                                     RecordQuery<I, R>> {

    private final Class<R> recordType;

    RecordQueryBuilder(Class<R> type) {
        recordType = type;
    }

    @Override
    protected RecordQueryBuilder<I, R> thisRef() {
        return this;
    }

    /**
     * Returns the type of record for which the query is being built.
     */
    public Class<R> recordType() {
        return recordType;
    }

    /**
     * Creates a new instance of {@link RecordQuery} basing on the data of this builder.
     */
    @Override
    public RecordQuery<I, R> build() {
        return new RecordQuery<>(this);
    }

    public <V> RecordCriterion<I, R, V> where(RecordColumn<R, V> column) {
        return new RecordCriterion<>(column, this);
    }
}
