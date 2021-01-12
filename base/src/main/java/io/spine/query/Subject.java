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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Message;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Set of criteria for the records obtained via querying.
 *
 * @param <I>
 *         the type of the identifiers of the queried records
 * @param <R>
 *         the type of the queried records
 */
public final class Subject<I, R extends Message> {

    /**
     * The type of the queried records.
     */
    private final Class<R> recordType;

    /**
     * The type of the identifiers of the queried records.
     */
    private final Class<I> idType;

    /**
     * The criteria put on the identifiers of the records of interest.
     */
    private final IdParameter<I> id;

    /**
     * Predicates, being the group of the parameters, against which the actual values
     * of target record fields are compared when querying.
     *
     * <p>The evaluation is done in a conjunction mode. I.e. a record matches the subject
     * if it matches each predicate.
     */
    private final ImmutableList<QueryPredicate<R>> predicates;

    Subject(QueryBuilder<I, R, ?, ?, ?> builder) {
        checkNotNull(builder);
        this.id = checkNotNull(builder.whichIds());
        this.idType = checkNotNull(builder.whichIdType());
        this.recordType = checkNotNull(builder.whichRecordType());
        this.predicates = checkNotNull(builder.predicates());
    }

    /**
     * Returns the type of the queried record.
     */
    public Class<R> recordType() {
        return recordType;
    }

    /**
     * Returns the type of the identifiers of the queried records.
     */
    public Class<I> idType() {
        return idType;
    }

    /**
     * Returns the criteria put on the identifiers of matched record.
     */
    public IdParameter<I> id() {
        return id;
    }

    /**
     * Returns the predicates for the fields of matched record.
     */
    public ImmutableList<QueryPredicate<R>> predicates() {
        return predicates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subject)) {
            return false;
        }
        Subject<?, ?> subject = (Subject<?, ?>) o;
        return id.equals(subject.id) &&
                recordType.equals(subject.recordType) &&
                predicates.equals(subject.predicates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recordType, predicates);
    }
}
