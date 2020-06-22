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

package io.spine.base.query;

import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.proto.FieldName;
import io.spine.value.ValueHolder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Alex Tymchenko
 */
public abstract class MessageColumn<M extends Message, V> extends ValueHolder<FieldName> {

    private static final long serialVersionUID = 0L;

    private final Class<M> messageType;
    private final Class<V> valueType;

    protected MessageColumn(String fieldName, Class<M> messageType, Class<V> valueType) {
        super(FieldName.of(fieldName));
        this.messageType = checkNotNull(messageType, "The type of the message must be set.");
        this.valueType = checkNotNull(valueType, "The type of the returning value must be set.");
    }

    @Internal
    public FieldName name() {
        return value();
    };

    @Internal
    public Class<M> entityStateType() {
        return messageType;
    }

    @Internal
    public Class<V> valueType() {
        return valueType;
    }
}
