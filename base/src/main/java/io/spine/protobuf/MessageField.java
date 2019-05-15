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

package io.spine.protobuf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * Abstract base for classes working with message fields.
 */
public abstract class MessageField implements Serializable {

    private static final long serialVersionUID = 0L;

    /** The prefix of generated getter methods for fields. */
    private static final String GETTER_METHOD_PREFIX = "get";

    /**
     * By convention underscore it is used for separating words in field names
     * of Protobuf messages.
     */
    private static final char PROPERTY_NAME_SEPARATOR = '_';

    /** A zero-based index of the field in a Protobuf message. */
    private final int index;

    /** A map from message class to a getter method of the corresponding Java class. */
    private static final Map<Class<? extends Message>, Method> accessors = Maps.newConcurrentMap();

    /**
     * Creates an instance for the field with the passed number.
     *
     * @param index the zero-based index of the field
     */
    protected MessageField(int index) {
        String message = "Message field index must be a positive value, encountered: ";
        checkArgument(index >= 0, message + index);
        this.index = index;
    }

    /** Returns a zero-based index of the field in a Protobuf message. */
    protected int getIndex() {
        return index;
    }

    /**
     * Obtains the value of the field from the passed message.
     *
     * @param message the message to get the field value from
     * @return field value
     * @throws MessageFieldException if the field is not available
     * @see #isFieldAvailable(Message)
     * @see #createUnavailableFieldException(Message)
     */
    public Object getValue(Message message) {
        if (!isFieldAvailable(message)) {
            throw createUnavailableFieldException(message);
        }

        Method method = getAccessor(message);
        try {
            Object result = method.invoke(message);
            return result;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw (MessageFieldException) new MessageFieldException(message).initCause(e);
        }
    }

    /**
     * Creates an exception for the case of a missing or incompatible field in the passed message.
     *
     * @param message   the message passed for obtaining value
     * @return new exception instance
     */
    protected abstract MessageFieldException createUnavailableFieldException(Message message);

    /**
     * Verifies if a field is available in the passed message.
     *
     * @param message the message to check
     * @return {@code true} if the field is available, {@code false} otherwise
     */
    protected abstract boolean isFieldAvailable(Message message);

    private Method getAccessor(Message message) {
        Class<? extends Message> messageClass = message.getClass();
        Method method = accessors.get(messageClass);

        if (method == null) {
            FieldDescriptor fieldDescriptor = getFieldDescriptor(message, this.index);
            String fieldName = fieldDescriptor.getName();
            String methodName = toAccessorMethodName(fieldName);

            try {
                method = messageClass.getMethod(methodName);
                method.setAccessible(true);
                accessors.put(messageClass, method);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        return method;
    }

    /**
     * Obtains a field descriptor for a field in the passed message.
     *
     * @param msg        the message to inspect
     * @param fieldIndex the index of the field
     * @return field descriptor
     */
    public static FieldDescriptor getFieldDescriptor(Message msg, int fieldIndex) {
        FieldDescriptor result = msg.getDescriptorForType()
                                    .getFields()
                                    .get(fieldIndex);
        return result;
    }

    /** Converts Protobuf field name into Java accessor method name. */
    @VisibleForTesting
    static String toAccessorMethodName(CharSequence fieldName) {
        StringBuilder out = new StringBuilder(checkNotNull(fieldName).length() + 3);
        out.append(GETTER_METHOD_PREFIX);
        char uppercaseFirstChar = Character.toUpperCase(fieldName.charAt(0));
        out.append(uppercaseFirstChar);

        boolean nextUpperCase = false;
        for (int i = 1; i < fieldName.length(); i++) {
            char ch = fieldName.charAt(i);
            if (PROPERTY_NAME_SEPARATOR == ch) {
                nextUpperCase = true;
                continue;
            }
            out.append(nextUpperCase ? Character.toUpperCase(ch) : ch);
            nextUpperCase = false;
        }

        return out.toString();
    }

    /**
     * Obtains Protobuf field name for the passed message.
     *
     * @param  msg   the message to inspect
     * @param  index a zero-based index of the field
     * @throws IndexOutOfBoundsException
     *         if the index is greater or equal the number of fields in the passed message
     * @return name of the field
     */
    public static String getFieldName(Message msg, int index) {
        FieldDescriptor fieldDescriptor = getFieldDescriptor(msg, index);
        String fieldName = fieldDescriptor.getName();
        return fieldName;
    }

    /**
     * Obtains a number of fields in the passed message.
     *
     * @param msg  the message to inspect
     * @return a positive number of fields or zero of the message type does not define fields
     */
    public static int getFieldCount(Message msg) {
        checkNotNull(msg);
        Descriptors.Descriptor descriptor = msg.getDescriptorForType();
        List<FieldDescriptor> fields = descriptor.getFields();
        return fields.size();

    }
}
