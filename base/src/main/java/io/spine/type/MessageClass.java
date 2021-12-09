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

package io.spine.type;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.value.ClassTypeValue;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A base class for value objects storing references to message classes.
 *
 * @param <M>
 *         type of message
 */
public abstract class MessageClass<M extends Message> extends ClassTypeValue<M> {

    private static final long serialVersionUID = 0L;

    /** The URL of the type of proto messages represented by this class. */
    private final TypeUrl typeUrl;

    protected MessageClass(Class<? extends M> value) {
        super(value);
        this.typeUrl = TypeUrl.of(value);
    }

    protected MessageClass(Class<? extends M> value, TypeUrl typeUrl) {
        super(value);
        this.typeUrl = checkNotNull(typeUrl);
    }

    /**
     * Obtains the type URL of messages of this class.
     */
    public TypeUrl typeUrl() {
        return typeUrl;    
    }

    /**
     * Obtains a type name of the messages of this class.
     */
    public TypeName typeName() {
        return typeUrl.toTypeName();
    }

    /**
     * Gathers all interfaces (extending {@link Message}) of the passed class,
     * and up in the hierarchy.
     *
     * <p>The {@link Message} interface is not included in the result.
     */
    public static ImmutableSet<Class<? extends Message>>
    interfacesOf(Class<? extends Message> cls) {
        checkNotNull(cls);
        ImmutableSet.Builder<Class<? extends Message>> builder = ImmutableSet.builder();
        var interfaces = cls.getInterfaces();
        Queue<Class<?>> deque = new ArrayDeque<>(Arrays.asList(interfaces));
        while (!deque.isEmpty()) {
            var anInterface = deque.poll();
            if (Message.class.isAssignableFrom(anInterface)
                    && !anInterface.equals(Message.class)) {
                @SuppressWarnings("unchecked")
                var cast = (Class<? extends Message>) anInterface;
                builder.add(cast);
            }
            interfaces = anInterface.getInterfaces();
            if (interfaces.length > 0) {
                deque.addAll(Arrays.asList(interfaces));
            }
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageClass)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var other = (MessageClass<?>) o;
        return typeUrl.equals(other.typeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeUrl);
    }
}
