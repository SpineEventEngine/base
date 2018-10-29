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
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;

import java.util.List;

import static io.spine.validate.FieldValidatorFactory.create;

/**
 * Validates messages according to Spine custom Protobuf options and
 * provides found constraint violations.
 */
@Internal
public class MessageValidator {

    private final MessageValue message;

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
        ImmutableList.Builder<ConstraintViolation> result = ImmutableList.builder();
        validateAlternativeFields(result);
        validateOneOfFields(result);
        validateFields(result);
        return result.build();
    }

    private void validateAlternativeFields(ImmutableList.Builder<ConstraintViolation> result) {
        AlternativeFieldValidator altFieldValidator = new AlternativeFieldValidator(message);
        result.addAll(altFieldValidator.validate());
    }

    /**
     * Validates fields except fields from {@code OneOf} declarations.
     *
     * <p>{@code OneOf} fields are validated {@linkplain #validateOneOfFields(ImmutableList.Builder)
     * separately}.
     *
     * @param result
     *         the builder of the message violations
     */
    private void validateFields(ImmutableList.Builder<ConstraintViolation> result) {
        for (FieldValue value : message.fieldsExceptOneOfs()) {
            FieldValidator<?> fieldValidator = create(value);
            List<ConstraintViolation> violations = fieldValidator.validate();
            result.addAll(violations);
        }
    }

    /**
     * Validates every {@code OneOf} declaration in the message.
     *
     * @param result
     *         the builder of the message violations
     */
    private void validateOneOfFields(ImmutableList.Builder<ConstraintViolation> result) {
        List<OneofDescriptor> oneOfDeclarations = message.oneOfs();
        for (OneofDescriptor oneOf : oneOfDeclarations) {
            OneOfValidator validator = new OneOfValidator(oneOf, message);
            ImmutableList<ConstraintViolation> oneOfViolations = validator.validate();
            result.addAll(oneOfViolations);
        }
    }
}
