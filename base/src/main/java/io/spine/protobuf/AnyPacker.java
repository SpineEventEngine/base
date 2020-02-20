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

package io.spine.protobuf;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.spine.type.TypeUrl;
import io.spine.type.UnexpectedTypeException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for packing messages into {@link Any} and unpacking them.
 *
 * <p>When packing, the {@code AnyPacker} takes care of obtaining correct type URL prefix
 * for the passed messages.
 *
 * <p>When unpacking, the {@code AnyPacker} obtains Java class matching the type URL
 * from the passed {@code Any}.
 *
 * @see Any#pack(Message, String)
 * @see Any#unpack(Class)
 */
public final class AnyPacker {

    /**
     * Prevents the utility class instantiation.
     */
    private AnyPacker() {
    }

    /**
     * Wraps {@link Message} object inside of {@link Any} instance.
     *
     * <p>If an instance of {@code Any} passed, this instance is returned.
     *
     * @param message the message to pack
     * @return the wrapping instance of {@link Any} or the message itself, if it is {@code Any}
     */
    public static Any pack(Message message) {
        if (message instanceof Any) {
            return (Any) message;
        }
        TypeUrl typeUrl = TypeUrl.from(message.getDescriptorForType());
        String typeUrlPrefix = typeUrl.prefix();
        Any result = Any.pack(message, typeUrlPrefix);
        return result;
    }

    /**
     * Unwraps {@code Any} value into an instance of type specified by value
     * returned by {@link Any#getTypeUrl()}.
     *
     * @param any instance of {@link Any} that should be unwrapped
     * @return unwrapped message instance
     */
    public static Message unpack(Any any) {
        checkNotNull(any);
        TypeUrl typeUrl = TypeUrl.ofEnclosed(any);
        Class<? extends Message> messageClass = typeUrl.getMessageClass();
        return unpack(any, messageClass);
    }

    /**
     * Unwraps {@code Any} value into an instance of the passed class.
     *
     * <p>If there is no Java class for the type, {@link UnexpectedTypeException
     * UnexpectedTypeException} is thrown.
     *
     * @param any
     *         instance of {@link Any} that should be unwrapped
     * @param cls
     *         the class implementing the type of the enclosed object
     * @param <T>
     *         the type enclosed into {@code Any}
     * @return unwrapped message instance
     * @implNote Protobuf {@link Any#unpack(Class)} isn't used by this
     *         implementation, since it creates a redundant default {@code Message} instance in its
     *         internal {@link Any#is(Class) is(Class)} sub-call. We are aiming for better
     *         performance and lower memory footprint. Therefore, we use the same default instance
     *         of the target {@code Message} to both verify the type name (complying
     *         {@code is(Class)} sub-call) and parse its contents.
     */
    public static <T extends Message> T unpack(Any any, Class<T> cls) {
        T defaultInstance = Messages.defaultInstance(cls);
        TypeUrl expectedTypeUrl = TypeUrl.of(defaultInstance);
        checkType(any, expectedTypeUrl);
        try {
            @SuppressWarnings("unchecked")  // Ensured by the check above.
            T result = (T) defaultInstance
                    .getParserForType()
                    .parseFrom(any.getValue());
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw new UnexpectedTypeException(e);
        }
    }

    /**
     * Creates an iterator that packs each incoming message into {@code Any}.
     *
     * @param iterator the iterator over messages to pack
     * @return the packing iterator
     */
    public static Iterator<Any> pack(Iterator<Message> iterator) {
        return new PackingIterator(iterator);
    }

    /**
     * Provides the function for unpacking messages from {@code Any}.
     *
     * <p>The function returns {@code null} for {@code null} input.
     */
    public static Function<@Nullable Any, @Nullable Message> unpackFunc() {
        return AnyPacker::unpackOrNull;
    }

    /**
     * Provides the function for unpacking messages of a given type from {@code Any}.
     *
     * <p>The function returns {@code null} for {@code null} input.
     *
     * <p>The function throws a {@link UnexpectedTypeException} if the actual type of the message
     * does not match the given class.
     *
     * @param type
     *         expected class of the messages
     */
    public static <T extends Message> Function<@Nullable Any, @Nullable T>
    unpackFunc(Class<T> type) {
        T defaultInstance = Messages.defaultInstance(type);
        @SuppressWarnings("unchecked")
        Parser<T> parser = (Parser<T>) defaultInstance.getParserForType();
        TypeUrl expectedTypeUrl = TypeUrl.of(defaultInstance);
        return any -> any == null
                      ? null
                      : parseMessage(parser, expectedTypeUrl, any);
    }

    private static <T extends Message> T
    parseMessage(Parser<T> parser, TypeUrl expectedTypeUrl, Any any) {
        checkType(any, expectedTypeUrl);
        try {
            T message = parser.parseFrom(any.getValue());
            return message;
        } catch (InvalidProtocolBufferException e) {
            throw new UnexpectedTypeException(e);
        }
    }

    private static void checkType(Any any, TypeUrl expectedType) {
        TypeUrl actualType = TypeUrl.ofEnclosed(any);
        if (!actualType.equals(expectedType)) {
            throw new UnexpectedTypeException(expectedType, actualType);
        }
    }

    private static @Nullable Message unpackOrNull(@Nullable Any any) {
        return any == null
               ? null
               : unpack(any);
    }
}
