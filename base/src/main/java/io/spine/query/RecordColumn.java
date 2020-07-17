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
import io.spine.annotation.Internal;
import io.spine.value.ValueHolder;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A queryable column of a stored record.
 *
 * <p>Defined for the records which are declared as Protobuf messages.
 *
 * @param <R>
 *         the type of the stored record
 * @param <V>
 *         the type of the column values
 */
public class RecordColumn<R extends Message, V>
        extends ValueHolder<ColumnName>
        implements Column<R, V> {

    private static final long serialVersionUID = 0L;

    private final Class<V> valueType;
    private final Getter<R, V> getter;

    /**
     * Creates a new instance of the {@code RecordColumn}.
     *
     * @param name
     *         the name of the column; must be non-empty
     * @param valueType
     *         the type of the column values
     * @param getter
     *         the getter returning the value of the column basing on the stored record
     */
    public RecordColumn(String name, Class<V> valueType, Getter<R, V> getter) {
        this(ColumnName.of(name), valueType, getter);
    }

    /**
     * Creates a new instance of the {@code RecordColumn}.
     *
     * @param name
     *         the name of the column
     * @param valueType
     *         the type of the column values
     * @param getter
     *         the getter returning the value of the column basing on the stored record
     */
    public RecordColumn(ColumnName name, Class<V> valueType, Getter<R, V> getter) {
        super(name);
        this.valueType = checkNotNull(valueType, "The type of the returning value must be set.");
        this.getter = checkNotNull(getter);
    }

    /**
     * Returns the name of the corresponding Protobuf message field.
     */
    @Override
    @Internal
    public ColumnName name() {
        return value();
    }

    /**
     * Returns the type of the column value.
     */
    @Override
    @Internal
    public Class<V> type() {
        return valueType;
    }

    /**
     * Obtains the value of this column for the passed message record.
     *
     * @see io.spine.query.Column.Getter
     */
    @Override
    public @Nullable V valueIn(R record) {
        return getter.apply(record);
    }
}
