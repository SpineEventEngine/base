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

import java.util.Objects;

/**
 * Defines the ordering of the {@linkplain Query query} results via the ordering of values in
 * a particular {@linkplain io.spine.query.RecordColumn column}.
 *
 * @param <C>
 *         type of the column which values are used for ordering
 * @param <R>
 *         the type of the ordered records
 */
public final class OrderBy<C extends RecordColumn<R, ?>, R extends Message> {

    private final C column;

    private final Direction direction;

    /**
     * Creates an ordering directive for the given column in a given direction.
     */
    OrderBy(C column, Direction direction) {
        this.column = column;
        this.direction = direction;
    }

    /**
     * Returns the column by which values the query should be ordered.
     */
    public C column() {
        return column;
    }

    /**
     * Returns the direction in which the column values should be ordered.
     */
    public Direction direction() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderBy)) {
            return false;
        }
        OrderBy<?, ?> by = (OrderBy<?, ?>) o;
        return column.equals(by.column) &&
                direction == by.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, direction);
    }
}
