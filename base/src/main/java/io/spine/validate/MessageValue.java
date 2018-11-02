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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Message;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A value of a {@link Message} to validate.
 */
final class MessageValue {

    private final Message message;
    private final Descriptor descriptor;
    private final FieldContext context;

    private MessageValue(Message message, FieldContext context) {
        this.message = checkNotNull(message);
        this.descriptor = message.getDescriptorForType();
        this.context = checkNotNull(context);
    }

    /**
     * Creates a value of a message field.
     *
     * @param messageContext
     *         the context of the field presented by the message
     * @param message
     *         the message itself
     * @return a new instance
     */
    static MessageValue nestedIn(FieldContext messageContext, Message message) {
        return new MessageValue(message, messageContext);
    }

    /**
     * Creates a value of a top-level message.
     *
     * @param message
     *         the message that is <b>not</b> a part of another message
     * @return a new instance
     */
    static MessageValue atTopLevel(Message message) {
        return new MessageValue(message, FieldContext.empty());
    }

    /**
     * Obtains field values of the message.
     *
     * <p>Values of {@code OneOf} fields are filtered and not returned.
     *
     * @return values of message fields exluding {@code OneOf} fields
     */
    ImmutableList<FieldValue> fieldsExceptOneOfs() {
        ImmutableList<FieldValue> values = descriptor.getFields()
                                                     .stream()
                                                     .filter(MessageValue::isNotOneOf)
                                                     .map(this::valueOf)
                                                     .collect(toImmutableList());
        return values;
    }

    /**
     * Obtains the value of the field with the specified name.
     *
     * @param fieldName
     *         the name of the field to obtain
     * @return a value of the field
     *         or {@code Optional.empty()} if the message doesn't contain the field
     */
    Optional<FieldValue> valueOf(String fieldName) {
        FieldDescriptor field = descriptor.findFieldByName(fieldName);
        return valueOfNullable(field);
    }

    /**
     * Obtains the value of a populated {@code OneOf} field.
     *
     * @param oneOf
     *         the {@code OneOf} descriptor
     * @return a value of the populated field or {@code Optional.empty()} if the message
     *         doesn't contain the field of it was not populated
     */
    Optional<FieldValue> valueOf(OneofDescriptor oneOf) {
        FieldDescriptor field = message.getOneofFieldDescriptor(oneOf);
        return valueOfNullable(field);
    }

    /** Returns options of the message. */
    Map<FieldDescriptor, Object> options() {
        Map<FieldDescriptor, Object> options = descriptor.getOptions()
                                                         .getAllFields();
        return options;
    }

    /** Returns descriptors of {@code OneOfs} in the message. */
    ImmutableList<OneofDescriptor> oneOfs() {
        return ImmutableList.copyOf(descriptor.getOneofs());
    }

    /** Returns the context of the message. */
    FieldContext context() {
        return context;
    }

    private Optional<FieldValue> valueOfNullable(@Nullable FieldDescriptor field) {
        if (field == null) {
            return Optional.empty();
        }
        FieldValue fieldValue = valueOf(field);
        return Optional.of(fieldValue);
    }

    private FieldValue valueOf(FieldDescriptor field) {
        FieldContext fieldContext = context.forChild(field);
        FieldValue value = FieldValue.of(message.getField(field), fieldContext);
        return value;
    }

    private static boolean isNotOneOf(FieldDescriptor field) {
        return field.getContainingOneof() == null;
    }
}
