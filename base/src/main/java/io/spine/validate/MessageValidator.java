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
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import io.spine.type.MessageType;

import java.util.List;
import java.util.Optional;

/**
 * Validates messages according to Spine custom Protobuf options and
 * provides found constraint violations.
 */
@Deprecated
@Internal
public class MessageValidator {

    private final MessageType type;
    private final Message message;

    private MessageValidator(MessageType type, Message message) {
        this.type = type;
        this.message = message;
    }

    /**
     * Validates the passed message against constraints defined in the message type.
     *
     * @return the list of violations or empty list if no violations are found.
     */
    public static List<ConstraintViolation> validate(Message message) {
        MessageValidator validator = newInstance(message);
        List<ConstraintViolation> result = validator.validate();
        return result;
    }

    /**
     * Creates a validator for a top-level message.
     *
     * @deprecated please use {@link #validate(Message)}
     */
    @Deprecated
    public static MessageValidator newInstance(Message message) {
        MessageType type = new MessageType(message.getDescriptorForType());
        return new MessageValidator(type, message);
    }

    /**
     * Validates messages according to Spine custom protobuf options and returns constraint
     * violations found.
     *
     * @deprecated please use {@link #validate(Message)}
     */
    @Deprecated
    public List<ConstraintViolation> validate() {
        Optional<ValidationError> error =
                Constraints.of(type)
                           .runThrough(new ConstraintInterpreter(message));
        List<ConstraintViolation> violations =
                error.map(ValidationError::getConstraintViolationList)
                     .orElse(ImmutableList.of());
        return violations;
    }
}
