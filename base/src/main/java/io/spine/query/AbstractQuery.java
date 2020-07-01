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
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An abstract base for queries which may be used to fetch the records defined as Protobuf messages.
 *
 * @param <I>
 *         the type of record identifiers
 * @param <R>
 *         the type of records
 * @param <P>
 *         the type of subject parameters used in a particular implementation
 */
public abstract class AbstractQuery<I, R extends Message, P extends SubjectParameter<?, ?>> {

    /**
     * Set of criteria defining the subject of querying.
     */
    private final Subject<I, P> subject;

    /**
     * List of ordering directives which define the order of records in the query results.
     *
     * <p>Directives are applied one by one, starting with the first one. The second one
     * and all consecutive directives specify the order of records, which are considered
     * equal by the previous {@code OrderBy} directives.
     */
    private final ImmutableList<OrderBy<?, R>> ordering;

    /**
     * The maximum number of records in the query results.
     *
     * <p>If not set, all matching records are returned.
     *
     * <p>This field may only be used if at least one {@link OrderBy ordering directive} is set.
     */
    private final @Nullable Integer limit;

    /**
     * Defines which fields are returned for the matching records.
     *
     * <p>If not set, the records are returned as-is.
     */
    @MonotonicNonNull
    private final @Nullable FieldMask mask;

    /**
     * A common contract for the constructors of {@code AbstractQuery} implementations.
     */
    AbstractQuery(AbstractQueryBuilder<I, R, P, ?, ?> builder) {
        this.subject = new Subject<>(builder.whichIds(), builder.predicates());
        this.ordering = builder.ordering();
        this.mask = builder.mask();
        limit = ensureLimit(builder.limit());
    }

    /**
     * Checks that if the limit is set, at least one ordering directive is specified as well.
     *
     * @return the value of query limit, {@code null}-able, as the limit may not be set
     */
    private @Nullable Integer ensureLimit(@Nullable Integer limit) {
        checkArgument(limit == null || !ordering.isEmpty(),
                      "Query limit must be used with at least one ordering directive set.");
        return limit;
    }

    /**
     * Returns the subject of querying.
     */
    public final Subject<I, P> subject() {
        return subject;
    }

    /**
     * Returns the ordering to be applied to the query results.
     *
     * <p>In case there are several fields to order by, the ordering directives are applied one
     * by one starting from the first.
     */
    public final ImmutableList<OrderBy<?, R>> ordering() {
        return ordering;
    }

    /**
     * Tells the maximum number of records to be returned as a query result.
     *
     * <p>If the limit is set, there must be at least one {@linkplain #ordering() ordering
     * directive} specified.
     *
     * <p>If the limit is not set, returns {@code null}.
     */
    public final @Nullable Integer limit() {
        return limit;
    }

    /**
     * Returns the field mask to be applied to each of the resulting records.
     */
    public FieldMask mask() {
        return mask;
    }
}
