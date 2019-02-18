/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.proto.ScalarType;
import io.spine.type.TypeName;
import io.spine.type.TypeUrl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utilities for working with {@link FieldPath} instances.
 */
public final class FieldPaths {

    private static final char SEPARATOR = '.';
    private static final Splitter dotSplitter = Splitter.on(SEPARATOR)
                                                        .trimResults();
    private static final Joiner joiner = Joiner.on(SEPARATOR);

    /** Prevents instantiation of this utility class. */
    private FieldPaths() {
    }

    /**
     * Parses the given field path into a {@link FieldPath}.
     *
     * @param stringPath
     *         non-empty field path
     * @return parsed field path
     */
    public static FieldPath parse(String stringPath) {
        checkNotNull(stringPath);
        checkArgument(!stringPath.isEmpty(), "Path must not be empty.");

        List<String> pathElements = dotSplitter.splitToList(stringPath);
        return fromElements(pathElements);
    }

    /**
     * Creates a new instance by the passed path elements.
     */
    @Internal
    public static FieldPath fromElements(List<String> elements) {
        checkNotNull(elements);
        checkArgument(!elements.isEmpty(), "Field path must contain at least one element.");
        FieldPath result = FieldPath
                .newBuilder()
                .addAllFieldName(elements)
                .build();
        return result;
    }

    /**
     * Obtains the value of the field at the given field path from the given value holder.
     *
     * <p>For example, if the given path is {@code protocol.name} and the given value holder is of
     * type {@link io.spine.net.Uri io.spine.net.Uri}, the method invocation is equivalent to
     * {@code uri.getSchema().getName()}.
     *
     * @param path
     *         non-empty field path
     * @param holder
     *         the message from which to obtain a value of the field
     * @return the value of the field
     */
    public static Object getValue(FieldPath path, Message holder) {
        checkNotNull(holder);
        checkNotNull(path);
        checkNotEmpty(path);
        Object result = getValue(path, holder, true);
        return result;
    }

    /**
     * Obtains a value of the field represented by the passed path in the passed message.
     *
     * @param path
     *         the path to the field in the message
     * @param holder
     *         the instance of message from which to obtain the value
     * @param strict
     *         If {@code true}, the method would fail with the {@code IllegalArgumentException}
     *         if there is no field matching the passed path.
     *         If {@code false}, and the field is not found, {@code null} will be returned
     * @return the value of the field, or
     *         {@code null} if the field was not found, and the {@code strict} parameter
     *         is {@code false}
     */
    private static @Nullable Object getValue(FieldPath path, Message holder, boolean strict) {
        Message message = holder;
        Object currentValue = message;
        for (Iterator<String> iterator = path.getFieldNameList().iterator(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            Descriptor type = message.getDescriptorForType();
            @Nullable FieldDescriptor field = type.findFieldByName(fieldName);
            if (field == null) {
                if (strict) {
                    throw newIllegalArgumentException(
                            "Unable to find the field named `%s` in the type `%s`.",
                            fieldName, type.getFullName());
                }
                return null;
            }
            currentValue = message.getField(field);
            if (currentValue instanceof Message) {
                message = (Message) currentValue;
            } else {
                if (iterator.hasNext()) {
                    /* We're not at the end of iteration, and the inner path item references a
                       non-message field. We cannot continue going the path and need to exit
                       the routine. */
                    if (strict) {
                        throw newIllegalArgumentException(
                                "The field referenced by the name `%s` is not a message, and" +
                                " it is not possible to obtain a nested field named `%s`. " +
                                " The full field path is: `%s`.",
                                fieldName,
                                iterator.next(),
                                toString(path)
                        );
                    }
                    return null;
                }
            }
        }
        return currentValue;
    }

    /**
     * Obtains a value referenced by the passed path in the passed message.
     *
     * @return the value of the referenced field, or empty {@code Optional} if the full path
     *         cannot be found
     */
    public static Optional<Object> find(FieldPath path, Message holder) {
        checkNotNull(path);
        checkNotNull(holder);
        Object result = getValue(path, holder, false);
        return Optional.ofNullable(result);
    }

    /**
     * Obtains string representation of the passed field path.
     */
    public static String toString(FieldPath path) {
        checkNotNull(path);
        String result = joiner.join(path.getFieldNameList());
        return result;
    }

    /**
     * Obtains the class of the field at the given field path from the given field holder type.
     *
     * @param holderType
     *         the type of the message to search
     * @param path
     *         the field path to search by
     * @return the class of the requested field
     */
    public static Class<?> typeOfFieldAt(Class<? extends Message> holderType, FieldPath path) {
        checkNotNull(holderType);
        checkNotNull(path);
        checkNotEmpty(path);

        Descriptor descriptor = TypeName.of(holderType).messageDescriptor();
        FieldDescriptor field = findField(path, descriptor);
        if (field == null) {
            throw newIllegalArgumentException(
                    "Unable to find a field referenced by the path `%s`" +
                            " in the message of type `%s`.",
                    toString(path),
                    TypeName.of(holderType)
            );
        }
        Class<?> result = classOf(field);
        return result;
    }

    /**
     * Obtains the field descriptor referenced by the path.
     */
    @Internal
    public static @Nullable FieldDescriptor findField(FieldPath path, Descriptor descriptor) {
        checkNotNull(path);
        checkNotNull(descriptor);
        Descriptor current = descriptor;
        FieldDescriptor field = null;
        for (Iterator<String> iterator = path.getFieldNameList().iterator(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            field = current.findFieldByName(fieldName);
            if (field == null) {
                return null;
            }
            if (iterator.hasNext()) {
                checkArgument(field.getType() == MESSAGE,
                              "Field `%s` of the type `%s` is not a message field.");
                current = field.getMessageType();
            }
        }
        return field;
    }

    private static void checkNotEmpty(FieldPath path) throws IllegalArgumentException {
        checkArgument(path.getFieldNameCount() > 0, "Field path must not be empty.");
    }

    private static Class<?> classOf(FieldDescriptor field) {
        Type type = field.getType();
        if (type == MESSAGE) {
            Class<?> cls = TypeUrl.from(field.getMessageType()).toJavaClass();
            return cls;
        } else if (type == ENUM) {
            Class<?> cls = TypeUrl.from(field.getEnumType()).toJavaClass();
            return cls;
        } else {
            Class<?> result = ScalarType.getJavaType(field.toProto().getType());
            return result;
        }
    }
}
