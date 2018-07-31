/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.type;

import com.google.protobuf.Message;
import io.spine.value.ClassTypeValue;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * A base class for value objects storing references to message classes.
 *
 * @author Alexander Yevsyukov
 */
public abstract class MessageClass extends ClassTypeValue<Message> {

    private static final long serialVersionUID = 0L;

    /** The name of the type of proto messages represented by this class. */
    private volatile @MonotonicNonNull TypeName typeName;

    protected MessageClass(Class<? extends Message> value) {
        super(value);
    }

    @SuppressWarnings("SynchronizeOnThis") // Double-check idiom for lazy init.
    public TypeName getTypeName() {
        TypeName result = typeName;
        if (result == null) {
            synchronized (this) {
                result = typeName;
                if (result == null) {
                    typeName = TypeName.of(value());
                    result = typeName;
                }
            }
        }
        return result;
    }
}
