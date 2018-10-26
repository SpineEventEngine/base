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

    private final FieldContext rootContext;

    /** Creates a new validator instance. */
    public static MessageValidator newInstance() {
        return new MessageValidator(FieldContext.empty());
    }

    /**
     * Creates a new validator instance.
     *
     * <p>Use this constructor for inner messages
     * (which are marked with "valid" option in Protobuf).
     *
     * @param rootContext the context of the message field,
     *                    which is the root for the messages to validate
     */
    static MessageValidator newInstance(FieldContext rootContext) {
        return new MessageValidator(rootContext);
    }

    private MessageValidator(FieldContext rootContext) {
        this.rootContext = rootContext;
    }

    /**
     * Validates messages according to Spine custom protobuf options and returns constraint
     * violations found.
     *
     * @param message a message to validate
     */
    public List<ConstraintViolation> validate(Message message) {
        ImmutableList.Builder<ConstraintViolation> result = ImmutableList.builder();
        validateAlternativeFields(message, result);
        validateFields(message, result);
        return result.build();
    }

    private void validateAlternativeFields(Message message,
                                           ImmutableList.Builder<ConstraintViolation> result) {
        Descriptor typeDescr = message.getDescriptorForType();
        AlternativeFieldValidator altFieldValidator =
                new AlternativeFieldValidator(typeDescr, rootContext);
        result.addAll(altFieldValidator.validate(message));
    }

    private void validateFields(Message message,
                                ImmutableList.Builder<ConstraintViolation> result) {
        Descriptor msgDescriptor = message.getDescriptorForType();
        List<FieldDescriptor> fields = msgDescriptor.getFields();
        for (FieldDescriptor field : fields) {
            FieldContext fieldContext = rootContext.forChild(field);
            FieldValue value = FieldValue.of(message.getField(field));
            FieldValidator<?> fieldValidator = create(fieldContext, value);
            List<ConstraintViolation> violations = fieldValidator.validate();
            result.addAll(violations);
        }
    }
}
