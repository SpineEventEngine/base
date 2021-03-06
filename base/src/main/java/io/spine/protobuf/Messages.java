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
package io.spine.protobuf;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.ProtocolMessageEnum;
import io.spine.annotation.Internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utility class for working with {@link Message} objects.
 */
public final class Messages {

    /** The name of a message builder factory method. */
    public static final String METHOD_NEW_BUILDER = "newBuilder";

    /**
     * The cache of the default instances per {@link Message} class.
     *
     * <p>Creates and caches objects in a lazy mode.
     */
    private static final
    LoadingCache<Class<? extends Message>, Message> defaultInstances = loadingCache(1_000);

    /** Prevent instantiation of this utility class. */
    private Messages() {
    }

    /**
     * Obtains the default instance of the passed message class.
     *
     * @param messageClass the class for which to obtain the default instance
     * @param <M> the type of the message
     * @return default instance of the class
     */
    public static <M extends Message> M defaultInstance(Class<M> messageClass) {
        checkNotNull(messageClass);
        @SuppressWarnings("unchecked")  // Ensured by the `MessageCacheLoader` implementation.
        M result = (M) defaultInstances.getUnchecked(messageClass);
        return result;
    }

    /**
     * Returns the builder for the passed message class.
     */
    @Internal
    public static Message.Builder builderFor(Class<? extends Message> cls) {
        checkNotNull(cls);
        try {
            Message message = defaultInstance(cls);
            Message.Builder builder = message.toBuilder();
            return builder;
        } catch (UncheckedExecutionException e) {
            String errMsg = format("Class `%s` must be a generated proto message.",
                                   cls.getCanonicalName());
            throw new IllegalArgumentException(errMsg, e);
        }
    }

    /**
     * Ensures that the passed instance of {@code Message} is not an {@code Any},
     * and unwraps the message if {@code Any} is passed.
     */
    public static Message ensureMessage(Message msgOrAny) {
        checkNotNull(msgOrAny);
        Message commandMessage;
        if (msgOrAny instanceof Any) {
            Any any = (Any) msgOrAny;
            commandMessage = AnyPacker.unpack(any);
        } else {
            commandMessage = msgOrAny;
        }
        return commandMessage;
    }

    private static LoadingCache<Class<? extends Message>, Message> loadingCache(int size) {
        return CacheBuilder.newBuilder()
                           .maximumSize(size)
                           .build(new MessageCacheLoader());
    }

    /**
     * Verifies if the passed message object is its default state and is not {@code null}.
     *
     * @param object
     *         the message to inspect
     * @return {@code true} if the message is in the default state, {@code false} otherwise
     */
    public static boolean isDefault(Message object) {
        checkNotNull(object);
        boolean result = object.getDefaultInstanceForType()
                               .equals(object);
        return result;
    }

    /**
     * Verifies if the passed message object is not its default state and is not {@code null}.
     *
     * @param object
     *         the message to inspect
     * @return {@code true} if the message is not in the default state, {@code false} otherwise
     */
    public static boolean isNotDefault(Message object) {
        checkNotNull(object);
        boolean result = !isDefault(object);
        return result;
    }

    /**
     * Verifies if the passed Protobuf enum element is the enum's default state.
     *
     * @param messageEnum
     *         the enum element to inspect
     * @return {@code true} if the passed enum is default, {@code false} otherwise
     */
    public static boolean isDefault(ProtocolMessageEnum messageEnum) {
        checkNotNull(messageEnum);
        return messageEnum.getNumber() == 0;
    }

    /**
     * Verifies if the passed Protobuf enum element is NOT the enum's default state.
     *
     * @param messageEnum
     *         the enum element to inspect
     * @return {@code false} if the passed enum is default, {@code true} otherwise
     */
    public static boolean isNotDefault(ProtocolMessageEnum messageEnum) {
        checkNotNull(messageEnum);
        return !isDefault(messageEnum);
    }

    /**
     * The loader of the cache of default instances per {@link Message} class.
     *
     * <p>Loads an default instance of {@code Message} for the given type passed.
     *
     * <p>The {@link MessageLite} is used as a super type of the loaded objects to comply
     * with {@linkplain com.google.protobuf.Internal Protobuf Internal} tool API.
     */
    private static final class MessageCacheLoader
            extends CacheLoader<Class<? extends Message>, Message> {

        @Override
        public Message load(Class<? extends Message> messageClass) {
            // It is safe to use the `Internal` utility class from Protobuf since it relies on the
            // the fact that the generated class has the `getDefaultInstance()` static method.
            return com.google.protobuf.Internal.getDefaultInstance(messageClass);
        }
    }
}
