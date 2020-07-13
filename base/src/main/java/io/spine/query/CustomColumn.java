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

import io.spine.annotation.SPI;

/**
 * A column storing the data which is not directly declared as a field in the {@code Message}
 * of an {@link io.spine.base.EntityState EntityState} or a plain record.
 *
 * <p>End-users may choose to store some arbitrary data along with the record.
 * E.g. the time of entity creation or the role of the user created the record etc, that is,
 * something not included into the definition of record {@code Message}.
 *
 * <p>To store the data like this, end-users would need to provide their own {@link Column}
 * implementation.
 *
 * @see CustomCriterion
 * @see QueryBuilder#addCustomParameter(CustomSubjectParameter)
 */
@SPI
public abstract class CustomColumn<R, V> implements Column<R, V> {

    /**
     * When building a query, creates a criterion for this column.
     *
     * @param builder
     *         a builder of the query
     * @param <B>
     *         the type of the query builder, in scope of which the created criterion will exist
     * @return a new criterion allowing to specify the desired value of this column when querying
     */
    <B extends QueryBuilder<?, ?, ?, B, ?>> CustomCriterion<R, V, B>
    in(B builder) {
        return new CustomCriterion<>(this, builder);
    }
}
