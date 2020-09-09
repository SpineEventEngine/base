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

import io.spine.base.EntityState;

/**
 * A parameter defining how to query a record of an entity state by the value
 * of its {@linkplain EntityColumn column}.
 *
 * @param <S>
 *         the type of entity state
 * @param <V>
 *         the type of the entity column values
 */
final class EntitySubjectParameter<S extends EntityState<?>, V>
        extends SubjectParameter<S, EntityColumn<S, V>, V> {

    /**
     * Creates an instance of the parameter targeting entities which column value is compared
     * to the one provided in a specified way.
     *
     * @param column
     *         the column to query
     * @param value
     *         the column value to use when querying
     * @param operator
     *         the operator to use when comparing the actual column value to the provided one
     */
    EntitySubjectParameter(EntityColumn<S, V> column, V value, ComparisonOperator operator) {
        super(column, operator, value);
    }
}
