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

package io.spine.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.annotation.Internal;
import io.spine.protobuf.AnyPacker;
import io.spine.string.StringifierRegistry;
import io.spine.type.TypeUrl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Wrapper of an identifier value.
 *
 * @param <I>
 *         type of the ID
 */
@Internal
public final class Identifier<I> {

    /** A {@code null} ID string representation. */
    public static final String NULL_ID = "NULL";

    /** An empty ID string representation. */
    static final String EMPTY_ID = "EMPTY";

    private final IdType type;
    private final I value;

    private Identifier(IdType type, I value) {
        this.value = value;
        this.type = type;
    }

    static <I> Identifier<I> from(I value) {
        checkNotNull(value);
        var type = IdType.of(value);
        var result = create(type, value);
        return result;
    }

    private static <I> Identifier<I> create(IdType type, I value) {
        return new Identifier<>(type, value);
    }

    private static Identifier<Message> fromMessage(Message value) {
        checkNotNull(value);
        var result = create(IdType.MESSAGE, value);
        return result;
    }

    /**
     * Obtains a default value for an identifier of the passed class.
     */
    public static <I> I defaultValue(Class<I> idClass) {
        checkNotNull(idClass);
        var type = toType(idClass);
        var result = type.defaultValue(idClass);
        return result;
    }

    /**
     * Obtains the type of this identifier.
     */
    @VisibleForTesting
    IdType type() {
        return type;
    }

    /**
     * Converts the class of identifiers to {@code Identifier.Type}.
     */
    public static <I> IdType toType(Class<I> idClass) {
        for (var type : IdType.values()) {
            if (type.matchClass(idClass)) {
                return type;
            }
        }
        throw unsupportedClass(idClass);
    }

    /**
     * Verifies if the passed value of the identifier is empty.
     *
     * <p>Always returns {@code false} for long and integer values.
     *
     * <p>For string and message identifiers, the method verifies the values.
     *
     * <p>A string identifier is empty, if it contains an empty string.
     *
     * @param value
     *         the value to check
     * @param <I>
     *         the type of the identifier
     * @return {@code true} if the identifier is empty;
     *         {@code false} otherwise
     */
    public static <I> boolean isEmpty(I value) {
        checkNotNull(value);
        var id = from(value);
        if (id.type == IdType.INTEGER || id.type == IdType.LONG) {
            return false;
        }

        var str = id.toString();
        var result = EMPTY_ID.equals(str);
        return result;
    }

    static <I> IllegalArgumentException unsupported(I id) {
        return newIllegalArgumentException("ID of unsupported type encountered: `%s`.", id);
    }

    private static <I> IllegalArgumentException unsupportedClass(Class<I> idClass) {
        return newIllegalArgumentException("Unsupported ID class encountered: `%s`.",
                                           idClass.getName());
    }

    /**
     * Ensures that the passed class of identifiers is supported.
     *
     * <p>The following types of IDs are supported:
     * <ul>
     *   <li>{@code String}
     *   <li>{@code Long}
     *   <li>{@code Integer}
     *   <li>A class implementing {@link Message}
     * </ul>
     *
     * <p>Consider using {@code Message}-based IDs if you want to have typed IDs in your code,
     * and/or if you need to have IDs with some structure inside.
     *
     * <p>Here are the examples of such structural IDs:
     * <ul>
     *   <li>EAN value used in bar codes
     *   <li>ISBN
     *   <li>Phone number
     *   <li>Email address as a couple of local-part and domain
     * </ul>
     *
     * @param <I>
     *         the type of the ID
     * @param idClass
     *         the class of IDs
     * @throws IllegalArgumentException
     *         if the class of IDs is not of supported type
     */
    public static <I> void checkSupported(Class<I> idClass) {
        checkNotNull(idClass);
        // Even through `getType()` can never return null, we use its return value here
        // instead of annotating the method so that the returned value can be ignored
        // just because of this one usage.
        var type = toType(idClass);
        checkNotNull(type);
    }

    /**
     * Wraps the passed ID value into an instance of {@link Any}.
     *
     * <p>The passed value must be of one of the supported types listed below.
     * The type of the value wrapped in to the returned instance is defined by the type
     * of the passed value:
     * <ul>
     *   <li>For classes implementing {@link Message} — the value of the message itself
     *   <li>For {@code String} — {@link StringValue}
     *   <li>For {@code Long} — {@link Int64Value}
     *   <li>For {@code Integer} — {@link Int32Value}
     * </ul>
     *
     * @param id
     *         the value to wrap
     * @param <I>
     *         the type of the value
     * @return instance of {@link Any} with the passed value
     * @throws IllegalArgumentException
     *         if the passed value is not of the supported type
     */
    public static <I> Any pack(I id) {
        checkNotNull(id);
        var identifier = from(id);
        var anyId = identifier.pack();
        return anyId;
    }

    /**
     * Extracts ID object from the passed {@code Any} instance.
     *
     * <p>Returned type depends on the type of the message wrapped into {@code Any}:
     * <ul>
     *   <li>{@code String} for unwrapped {@link StringValue}
     *   <li>{@code Integer} for unwrapped {@link Int32Value}
     *   <li>{@code Long} for unwrapped {@link Int64Value}
     *   <li>unwrapped {@code Message} instance if its type is none of the above
     * </ul>
     *
     * @param any
     *         the ID value wrapped into {@code Any}
     * @return unwrapped ID
     */
    public static Object unpack(Any any) {
        checkNotNull(any);
        var unpacked = AnyPacker.unpack(any);
        for (var type : IdType.values()) {
            if (type.matchMessage(unpacked)) {
                var result = type.fromMessage(unpacked);
                return result;
            }
        }
        /*
            This branch is highly unlikely because of the following:
             1) `StringValue`, `Int32Value`, `Int64Value` are covered by `IdType.STRING`,
                `IdType.INTEGER`, and `IdType.LONG` correspondingly. They would "intercept" an
                unpacked value in the `for` loop above.
             2) The `IdType.MESSAGE` accepts (!) all the types but `StringValue`, `Int32Value`,
                or `Int64Value`. It does so because it does not "intercept" the message-based value
                of another "primitive" type of identifiers. That's why anything like `BooleanValue`,
                or event `Empty` would be recognized as valid `Message`-based identifier. And we
                want to keep it this way for flexibility. E.g. someone may want to arrange
                a singleton `ProcessManager` having `Empty` as an identifier. So be it!
        */
        throw unsupported(unpacked);
    }

    /**
     * Does the same as {@link #unpack(com.google.protobuf.Any)} and
     * additionally casts the ID to the specified class.
     *
     * @param any
     *         the ID value wrapped into {@code Any}
     * @param idClass
     *         the class of the packed ID
     * @param <I>
     *         the type of the packed ID
     * @return unwrapped ID
     */
    public static <I> I unpack(Any any, Class<I> idClass) {
        checkNotNull(idClass);
        var identifier = unpack(any);
        return idClass.cast(identifier);
    }

    /**
     * Generates a new random UUID.
     *
     * @return the generated value
     * @see UUID#randomUUID()
     */
    public static String newUuid() {
        var id = UUID.randomUUID()
                     .toString();
        return id;
    }

    /**
     * Converts the passed ID value into the string representation.
     *
     * @param id
     *         the value to convert
     * @param <I>
     *         the type of the ID
     * @return <ul>
     *         <li>for classes implementing {@link Message} — a JSON form;
     *           <li>for {@code String}, {@code Long}, {@code Integer} —
     *               the result of {@link Object#toString()};
     *           <li>for {@code null} ID — the {@link #NULL_ID};
     *           <li>if the result is empty or a blank string — the {@link #EMPTY_ID}.
     *         </ul>
     * @throws IllegalArgumentException
     *         if the passed type isn't one of the above or
     *         the passed {@link Message} instance has no fields
     * @see StringifierRegistry
     */
    public static <I> String toString(@Nullable I id) {
        if (id == null) {
            return NULL_ID;
        }

        Identifier<?> identifier;
        if (id instanceof Any) {
            var unpacked = AnyPacker.unpack((Any) id);
            identifier = fromMessage(unpacked);
        } else {
            identifier = from(id);
        }

        var result = identifier.toString();
        return result;
    }

    private Any pack() {
        var result = type.pack(value);
        return result;
    }

    /**
     * Finds the first ID field of the specified type in the passed message type.
     *
     * @param idClass
     *          the class of identifiers
     * @param message
     *          the descriptor of the message type in which to find a field
     * @param <I>
     *          the type of identifiers
     * @return the descriptor of the matching field or
     *         empty {@code Optional} if there is no such a field
     */
    public static <I> Optional<FieldDescriptor> findField(Class<I> idClass, Descriptor message) {
        checkNotNull(idClass);
        checkNotNull(message);
        var idType = toType(idClass);
        var found =
                message.getFields()
                       .stream()
                       .filter(idType::matchField)
                       .filter(f -> idType != IdType.MESSAGE || sameType(idClass, f))
                       .findFirst();
        return found;
    }

    /**
     * Verifies if the class of identifiers and the type of the field represent the same type.
     */
    private static <I> boolean sameType(Class<I> idClass, FieldDescriptor f) {
        @SuppressWarnings("unchecked") // safe since it's Message type.
        var messageType = TypeUrl.of((Class<? extends Message>) idClass);
        var fieldType = TypeUrl.from(f.getMessageType());
        return fieldType.equals(messageType);
    }

    @Override
    public String toString() {
        String result;
        switch (type) {
            case INTEGER:
            case LONG:
            case STRING:
                result = value.toString();
                break;
            case MESSAGE:
                result = MessageIdToString.toString((Message) value);
                break;
            default:
                throw newIllegalStateException(
                        "`toString()` is not supported for type: `%s`.", type
                );
        }
        if (result.isEmpty()) {
            result = EMPTY_ID;
        }
        return result;
    }
}
