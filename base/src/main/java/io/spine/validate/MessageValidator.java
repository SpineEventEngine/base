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
import com.google.protobuf.Message;
import io.spine.annotation.Internal;

import java.util.List;

import static io.spine.validate.FieldValidatorFactory.create;

/**
 * Validates messages according to Spine custom protobuf options and provides constraint
 * violations found.
 *
 * @author Alexander Litus
 */
@Internal
public class MessageValidator {

    private final Message message;
    private final FieldContext messageContext;

    private MessageValidator(Message message, FieldContext messageContext) {
        this.message = message;
        this.messageContext = messageContext;
    }

    /**
     * Creates a validator for a top-level message.
     */
    public static MessageValidator newInstance(Message message) {
        return new MessageValidator(message, FieldContext.empty());
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
        return new MessageValidator(message, messageContext);
    }

    /**
     * Validates messages according to Spine custom protobuf options and returns constraint
     * violations found.
     */
    public List<ConstraintViolation> validate() {
        ImmutableList.Builder<ConstraintViolation> result = ImmutableList.builder();
        validateAlternativeFields(message, result);
        validateFields(message, result);
        return result.build();
    }

    private void validateAlternativeFields(Message message,
                                           ImmutableList.Builder<ConstraintViolation> result) {
        AlternativeFieldValidator altFieldValidator =
                new AlternativeFieldValidator(message, messageContext);
        result.addAll(altFieldValidator.validate());
    }

    private void validateFields(Message message,
                                ImmutableList.Builder<ConstraintViolation> result) {
        Descriptor msgDescriptor = message.getDescriptorForType();
        List<FieldDescriptor> fields = msgDescriptor.getFields();
        for (FieldDescriptor field : fields) {
            FieldContext fieldContext = messageContext.forChild(field);
            FieldValue value = FieldValue.of(message.getField(field), fieldContext);
            FieldValidator<?> fieldValidator = create(value);
            List<ConstraintViolation> violations = fieldValidator.validate();
            result.addAll(violations);
        }
    }
}
