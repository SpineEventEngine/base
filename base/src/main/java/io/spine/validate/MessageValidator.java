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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.code.proto.FieldContext;

import java.util.List;

/**
 * Validates messages according to Spine custom Protobuf options and
 * provides found constraint violations.
 */
@Internal
public class MessageValidator {

    private final MessageValue message;
    private final ImmutableList.Builder<ConstraintViolation> result = ImmutableList.builder();

    private MessageValidator(MessageValue message) {
        this.message = message;
    }

    /**
     * Creates a validator for a top-level message.
     */
    public static MessageValidator newInstance(Message message) {
        MessageValue messageValue = MessageValue.atTopLevel(message);
        return new MessageValidator(messageValue);
    }

    /**
     * Creates a validator for a message inside another message.
     *
     * @param message
     *         the message to validate
     * @param messageContext
     *         the context of the message
     */
    static MessageValidator newInstance(Message message, FieldContext messageContext) {
        MessageValue messageValue = MessageValue.nestedIn(messageContext, message);
        return new MessageValidator(messageValue);
    }

    /**
     * Validates messages according to Spine custom protobuf options and returns constraint
     * violations found.
     */
    public List<ConstraintViolation> validate() {
        validateAlternativeFields();
        validateOneofFields();
        validateFields();
        validateGoesWithFields();
        return result.build();
    }

    private void validateGoesWithFields() {
        GoesWithValidator goesWithValidator = new GoesWithValidator(message);
        result.addAll(goesWithValidator.validate());
    }

    private void validateAlternativeFields() {
        AlternativeFieldValidator altFieldValidator = new AlternativeFieldValidator(message);
        result.addAll(altFieldValidator.validate());
    }

    /**
     * Validates fields except fields from {@code Oneof} declarations.
     *
     * <p>{@code Oneof} fields are validated {@linkplain #validateOneofFields()
     * separately}.
     */
    private void validateFields() {
        for (FieldValue value : message.fieldsExceptOneofs()) {
            FieldValidator<?> fieldValidator = value.createValidator();
            List<ConstraintViolation> violations = fieldValidator.validate();
            result.addAll(violations);
        }
    }

    /**
     * Validates every {@code Oneof} declaration in the message.
     */
    private void validateOneofFields() {
        List<OneofDescriptor> oneofDescriptors = message.oneofDescriptors();
        for (OneofDescriptor oneof : oneofDescriptors) {
            OneofValidator validator = new OneofValidator(oneof, message);
            ImmutableList<ConstraintViolation> oneofViolations = validator.validate();
            result.addAll(oneofViolations);
        }
    }
}
