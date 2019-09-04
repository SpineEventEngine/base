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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;
import io.spine.annotation.Internal;
import io.spine.type.TypeName;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utilities for working with {@link FieldPath} instances.
 *
 * @deprecated please use {@link Field} instead
 */
@Deprecated
public final class FieldPaths {

    /** Prevents instantiation of this utility class. */
    private FieldPaths() {
    }

    /**
     * Parses the given field path into a {@link FieldPath}.
     *
     * @param fieldPath
     *         non-empty field path
     * @return parsed field path
     * @deprecated please use {@link Field#parse(String)}
     */
    @Deprecated
    public static FieldPath parse(String fieldPath) {
        checkNotNull(fieldPath);
        return Field.doParse(fieldPath);
    }

    /**
     * Creates a new instance by the passed path elements.
     *
     * @deprecated please use {@link Field#named(String)} and then {@link Field#path()}.
     */
    @Deprecated
    @Internal
    public static FieldPath fromElements(List<String> elements) {
        return Field.create(elements);
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
     * @throws IllegalArgumentException if the passed message does not define such a field
     * @deprecated please use {@link Field#withPath(FieldPath)} and then {@link Field#findValue(Message)}
     */
    @Deprecated
    public static Object getValue(FieldPath path, Message holder) {
        checkNotNull(holder);
        checkNotNull(path);
        Field.checkNotEmpty(path);
        Object result = Field.doGetValue(path, holder, true);
        return result;
    }

    /**
     * Obtains a value referenced by the passed path in the passed message.
     *
     * @return the value of the referenced field, or empty {@code Optional} if the full path
     *         cannot be found
     * @deprecated please use {@link Field#withPath(FieldPath)} and then {@link Field#findValue(Message)}
     */
    @Deprecated
    public static Optional<Object> find(FieldPath path, Message holder) {
        checkNotNull(path);
        checkNotNull(holder);
        Object result = Field.doGetValue(path, holder, false);
        return Optional.ofNullable(result);
    }

    /**
     * Obtains string representation of the passed field path.
     *
     * @deprecated please use {@link Field#withPath(FieldPath)} and then {@link Field#toString()}
     */
    @Deprecated
    public static String toString(FieldPath path) {
        checkNotNull(path);
        ProtocolStringList names = path.getFieldNameList();
        return Field.join(names);
    }

    /**
     * Obtains the class of the field at the given field path from the given field holder type.
     *
     * @param holderType
     *         the type of the message to search
     * @param path
     *         the field path to search by
     * @return the class of the requested field
     * @deprecated please use {@link Field#withPath(FieldPath)} and then {@link Field#findType(Class)}
     */
    @Deprecated
    public static Class<?> typeOfFieldAt(Class<? extends Message> holderType, FieldPath path) {
        checkNotNull(holderType);
        checkNotNull(path);
        Field.checkNotEmpty(path);
        Descriptor descriptor = TypeName.of(holderType).messageDescriptor();
        @Nullable FieldDescriptor field = findField(path, descriptor);
        if (field == null) {
            throw newIllegalArgumentException(
                    "Unable to find a field referenced by the path `%s`" +
                            " in the message of type `%s`.",
                    toString(path),
                    TypeName.of(holderType)
            );
        }
        Class<?> result = Field.classOf(field);
        return result;
    }

    /**
     * Obtains the field descriptor referenced by the path.
     *
     * @deprecated please use {@link Field#withPath(FieldPath)} and then
     * {@link Field#findDescriptor(com.google.protobuf.Descriptors.Descriptor) Field.descriptorIn()}
     */
    @Deprecated
    public static @Nullable FieldDescriptor findField(FieldPath path, Descriptor descriptor) {
        checkNotNull(path);
        checkNotNull(descriptor);
        return Field.fieldIn(path, descriptor);
    }
}
