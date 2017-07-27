/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

    private final DescriptorPath rootDescriptorPath;

    /** Creates a new validator instance. */
    public static MessageValidator newInstance() {
        return new MessageValidator(DescriptorPath.empty());
    }

    /**
     * Creates a new validator instance.
     *
     * <p>Use this constructor for inner messages
     * (which are marked with "valid" option in Protobuf).
     *
     * @param rootDescriptorPath the descriptor path to the message field,
     *                           which is the root for this message
     */
    static MessageValidator newInstance(DescriptorPath rootDescriptorPath) {
        return new MessageValidator(rootDescriptorPath);
    }

    private MessageValidator(DescriptorPath rootDescriptorPath) {
        this.rootDescriptorPath = rootDescriptorPath;
    }

    /**
     * Validates messages according to Spine custom protobuf options and returns constraint
     * violations found.
     *
     * @param message a message to validate
     */
    public List<ConstraintViolation> validate(Message message) {
        final ImmutableList.Builder<ConstraintViolation> result = ImmutableList.builder();
        validateAlternativeFields(message, result);
        validateFields(message, result);
        return result.build();
    }

    private void validateAlternativeFields(Message message,
                                           ImmutableList.Builder<ConstraintViolation> result) {
        final Descriptor typeDescr = message.getDescriptorForType();
        final AlternativeFieldValidator altFieldValidator =
                new AlternativeFieldValidator(typeDescr, rootDescriptorPath);
        result.addAll(altFieldValidator.validate(message));
    }

    private void validateFields(Message message,
                                ImmutableList.Builder<ConstraintViolation> result) {
        final Descriptor msgDescriptor = message.getDescriptorForType();
        final List<FieldDescriptor> fields = msgDescriptor.getFields();
        for (FieldDescriptor field : fields) {
            final DescriptorPath fieldDescriptorPath = rootDescriptorPath.forChild(field);
            final Object value = message.getField(field);
            final FieldValidator<?> fieldValidator = create(fieldDescriptorPath, value);
            final List<ConstraintViolation> violations = fieldValidator.validate();
            result.addAll(violations);
        }
    }
}
