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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Message;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * A column of a record residing in a storage.
 *
 * <p>Columns are the values of record fields stored along with the record itself. They are used
 * for filtering the results when querying the storage.
 *
 * @param <R>
 *         the type of records
 * @param <V>
 *         the type of column values
 */
public interface Column<R, V> {

    /**
     * The name of the column.
     */
    ColumnName name();

    /**
     * The type of the column value.
     */
    Class<V> type();

    /**
     * Returns the value of the column in a source record.
     */
    @Nullable V valueIn(R source);

    /**
     * A method object serving to obtain the value of the column for some particular record of the
     * matching type.
     *
     * @param <R>
     *         the type of records
     * @param <V>
     *         the type of column values
     */
    @Immutable
    @FunctionalInterface
    interface Getter<R extends Message, V> extends Function<R, V> {
    }
}
