/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.validate;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Message;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.OneofDeclaration;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A value of a {@link Message} to validate.
 *
 * @implNote For the performance reasons the values of the passed {@code Message} fields are
 *         read either via {@linkplain Message#getField(com.google.protobuf.Descriptors.FieldDescriptor)
 *         reflection} or {@linkplain FieldAwareMessage#readValue(com.google.protobuf.Descriptors.FieldDescriptor)
 *         directly} if an instance of {@link FieldAwareMessage} is passed.
 *         Also, for the same reason the contents of non-{@code oneof} fields are cached once read.
 */
@Immutable
public final class MessageValue {

    /**
     * The message which is validated.
     */
    private final Message message;

    /**
     * Refers to a {@code Message} which may optimize reading of its fields.
     *
     * <p>If the {@linkplain MessageValue#MessageValue(Message, FieldContext) passed} message isn't
     * an instance of {@link FieldAwareMessage}, this field is {@code null}.
     */
    private final @Nullable FieldAwareMessage asFieldAware;

    /**
     * The descriptor of type of the validated message.
     */
    private final Descriptor descriptor;

    /**
     * The field context of the validated message.
     */
    private final FieldContext context;

    private MessageValue(Message message, FieldContext context) {
        this.message = message;
        this.descriptor = message.getDescriptorForType();
        this.context = context;
        if (message instanceof FieldAwareMessage) {
            asFieldAware = (FieldAwareMessage) message;
        } else {
            asFieldAware = null;
        }
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
    public static MessageValue nestedIn(FieldContext messageContext, Message message) {
        checkNotNull(message);
        checkNotNull(messageContext);
        return new MessageValue(message, messageContext);
    }

    /**
     * Creates a value of a top-level message.
     *
     * @param message
     *         the message that is <b>not</b> a part of another message
     * @return a new instance
     */
    public static MessageValue atTopLevel(Message message) {
        checkNotNull(message);
        return new MessageValue(message, FieldContext.empty());
    }

    public MessageType declaration() {
        return new MessageType(descriptor);
    }

    /**
     * Obtains the value of the field with the specified name.
     *
     * @param fieldName
     *         the name of the field to obtain
     * @return a value of the field
     *         or {@code Optional.empty()} if the message doesn't contain the field
     */
    public Optional<FieldValue> valueOf(String fieldName) {
        var field = descriptor.findFieldByName(fieldName);
        return valueOf(field);
    }

    /**
     * Obtains the value of the field with the specified field descriptor.
     *
     * @param fieldDescriptor
     *         the field descriptor of the field to obtain
     * @return a value of the field or {@code Optional.empty()} if the message doesn't contain
     * the field
     */
    public Optional<FieldValue> valueOf(FieldDescriptor fieldDescriptor) {
        return valueOfNullable(fieldDescriptor);
    }

    /**
     * Obtains the value of the field with the specified field declaration.
     *
     * @param field
     *         the declaration of the field to obtain
     * @return a value of the field
     */
    public FieldValue valueOf(FieldDeclaration field) {
        checkNotNull(field);
        return valueOfField(field.descriptor());
    }

    /**
     * Obtains the value of a populated {@code oneof} field.
     *
     * @param oneof
     *         the {@code oneof} descriptor
     * @return a value of the populated field
     *         or {@code Optional.empty()} if the field was not populated
     * @throws IllegalArgumentException
     *         if the if the message doesn't declare this oneof
     */
    public Optional<FieldValue> valueOf(OneofDescriptor oneof) {
        checkArgument(descriptor.getOneofs()
                                .contains(oneof));
        var field = message.getOneofFieldDescriptor(oneof);
        return valueOfNullable(field);
    }

    /**
     * Obtains the value of a populated {@code oneof} field.
     *
     * @param oneof
     *         the {@code oneof} declaration
     * @return a value of the populated field
     *         or {@code Optional.empty()} if the field was not populated
     * @throws IllegalArgumentException
     *         if the if the message doesn't declare this oneof
     */
    public Optional<FieldValue> valueOf(OneofDeclaration oneof) {
        return valueOf(oneof.descriptor());
    }

    /** Returns the context of the message. */
    @SuppressWarnings("unused")
    FieldContext context() {
        return context;
    }

    private Optional<FieldValue> valueOfNullable(@Nullable FieldDescriptor field) {
        if (field == null) {
            return Optional.empty();
        }
        var fieldValue = valueOfField(field);
        return Optional.of(fieldValue);
    }

    private FieldValue valueOfField(FieldDescriptor field) {
        var fieldContext = context.forChild(field);
        var rawValue = readValue(field);
        @SuppressWarnings("Immutable") // field values are immutable
        var value = FieldValue.of(rawValue, fieldContext);
        return value;
    }

    private Object readValue(FieldDescriptor field) {
        return asFieldAware == null ? message.getField(field) : asFieldAware.readValue(field);
    }
}
