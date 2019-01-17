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

import java.util.List;

/**
 * An option that validates a message.
 *
 * <p>Defines the set of rules against which the message can be validated.
 *
 * @param <T>
 *         type of information held by this option
 */
public abstract class MessageValidatingOption<T> implements ValidatingOption<T, MessageValue> {

    @Override
    public List<ConstraintViolation> validateAgainst(MessageValue something) {
        if (optionPresent(something)) {
            return applyValidationRules(something);
        }
        return ImmutableList.of();
    }

    /**
     * Defines the rules according to which a message will be validated.
     *
     * @param message
     *         a message that is being validated.
     * @return a set of constraints that the message violates
     */
    abstract List<ConstraintViolation> applyValidationRules(MessageValue message);

    /** Returns whether this option is present for the specified message. */
    abstract boolean optionPresent(MessageValue something);
}
