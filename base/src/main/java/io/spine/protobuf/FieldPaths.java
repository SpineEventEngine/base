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

package io.spine.protobuf;

import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;
import io.spine.base.FieldPath;
import io.spine.code.proto.ScalarType;
import io.spine.type.TypeName;
import io.spine.type.TypeUrl;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

/**
 * Utilities for working with {@link io.spine.base.FieldPath} instances.
 *
 * @author Dmytro Dashenkov
 */
public final class FieldPaths {

    private static final Splitter dotSplitter = Splitter.on('.').trimResults();

    /**
     * Prevents the utility class instantiation.
     */
    private FieldPaths() {
    }

    /**
     * Parses the given field path into a {@link io.spine.base.FieldPath}.
     *
     * @param stringPath
     *         non-empty field path
     * @return parsed field path
     */
    public static FieldPath parse(String stringPath) {
        checkNotNull(stringPath);
        checkArgument(!stringPath.isEmpty(), "Path must not be empty.");

        List<String> pathElements = dotSplitter.splitToList(stringPath);
        FieldPath result = FieldPath
                .newBuilder()
                .addAllFieldName(pathElements)
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
     * @param holder
     *         message to obtain the (nested) field value from
     * @param path
     *         non-empty field path
     * @return the value of the field
     */
    public static Object fieldAt(Message holder, FieldPath path) {
        checkNotNull(holder);
        checkNotNull(path);
        checkNotEmpty(path);

        Message message = holder;
        Object currentValue = message;
        for (Iterator<String> iterator = path.getFieldNameList().iterator(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            FieldDescriptor field = getField(message.getDescriptorForType(), fieldName);
            currentValue = message.getField(field);
            if (currentValue instanceof Message) {
                message = (Message) currentValue;
            } else {
                checkArgument(!iterator.hasNext(), "%s is not a message.", currentValue);
            }
        }
        return currentValue;
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

        Descriptor descriptor = TypeName.of(holderType).getMessageDescriptor();
        for (Iterator<String> iterator = path.getFieldNameList().iterator(); iterator.hasNext(); ) {
            String fieldName = iterator.next();
            FieldDescriptor field = getField(descriptor, fieldName);
            if (iterator.hasNext()) {
                checkArgument(field.getType() == MESSAGE,
                              "Field %s of type %s is not a message field.");
                descriptor = field.getMessageType();
            } else {
                return classOf(field);
            }
        }
        throw new IllegalStateException("Unreachable statement.");
    }

    private static void checkNotEmpty(FieldPath path) throws IllegalArgumentException {
        checkArgument(path.getFieldNameCount() > 0, "Field path must not be empty.");
    }

    private static FieldDescriptor getField(Descriptor container, String name) {
        FieldDescriptor field = container.findFieldByName(name);
        checkArgument(field != null, "Field `%s` not found.", name);
        return field;
    }

    private static Class<?> classOf(FieldDescriptor field) {
        Type type = field.getType();
        if (type == MESSAGE) {
            Class<?> cls = TypeUrl.from(field.getMessageType()).getJavaClass();
            return cls;
        } else if (type == ENUM) {
            Class<?> cls = TypeUrl.from(field.getEnumType()).getJavaClass();
            return cls;
        } else {
            Class<?> result = ScalarType.getJavaType(field.toProto().getType());
            return result;
        }
    }
}
