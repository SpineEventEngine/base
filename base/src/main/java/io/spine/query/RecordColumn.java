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
import io.spine.annotation.Internal;
import io.spine.value.ValueHolder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

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
public class RecordColumn<R extends Message, V> extends ValueHolder<ColumnName> {

    private static final long serialVersionUID = 0L;

    private final Class<R> messageType;
    private final Class<V> valueType;
    private final Getter<R, V> getter;

    protected RecordColumn(String name,
                           Class<R> recordType,
                           Class<V> valueType,
                           Getter<R, V> getter) {
        super(ColumnName.of(name));
        this.messageType = checkNotNull(recordType, "The type of the record must be set.");
        this.valueType = checkNotNull(valueType, "The type of the returning value must be set.");
        this.getter = checkNotNull(getter);
    }

    /**
     * Returns the name of the corresponding Protobuf message field.
     */
    @Internal
    public ColumnName name() {
        return value();
    }

    /**
     * Returns the type of enclosing Protobuf message.
     */
    @Internal
    public Class<R> enclosingMessageType() {
        return messageType;
    }

    /**
     * Returns the type of the column value.
     */
    @Internal
    public Class<V> valueType() {
        return valueType;
    }

    /**
     * Obtains the value of this column for the passed message record.
     *
     * @see Getter
     */
    public @Nullable V valueIn(R record) {
        return getter.apply(record);
    }

    /**
     * A method object serving to obtain the value of the column for some particular record of the
     * matching type.
     *
     * @param <M>
     *         the type of the message record
     * @param <V>
     *         the type of the column value
     */
    @Immutable
    @FunctionalInterface
    public interface Getter<R extends Message, V> extends Function<R, V> {
    }
}
