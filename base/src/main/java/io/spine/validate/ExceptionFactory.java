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
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.Value;
import io.spine.annotation.Internal;
import io.spine.base.Error;
import io.spine.type.MessageClass;
import io.spine.validate.diags.ViolationText;

import java.util.Map;

import static java.lang.String.format;

/**
 * A helper class for building exceptions used to report invalid {@code Message}s,
 * which have fields that violate validation constraint(s).
 *
 * @param <E> type of {@code Exception} to build
 * @param <M> type of the {@code Message}
 * @param <C> type of the {@linkplain io.spine.type.MessageClass} of {@code |M|}.
 * @param <R> type of an error code to use for error reporting; must be a Protobuf enum value
 */
@SuppressWarnings("unused")
@Internal
public abstract class ExceptionFactory<E extends Exception,
                                       M extends Message,
                                       C extends MessageClass<?>,
                                       R extends ProtocolMessageEnum> {

    private final ImmutableList<ConstraintViolation> constraintViolations;
    private final M message;

    /**
     * Creates an {@code ExceptionFactory} instance for a given message and
     * constraint violations.
     *
     * @param message
     *         an invalid event message
     * @param violations
     *         constraint violations for the event message
     */
    protected ExceptionFactory(M message, Iterable<ConstraintViolation> violations) {
        this.constraintViolations = ImmutableList.copyOf(violations);
        this.message = message;
    }

    /**
     * Obtains a {@code MessageClass} for an invalid {@code Message}.
     */
    protected abstract C getMessageClass();

    /**
     * Obtains an error code to use for error reporting.
     */
    protected abstract R getErrorCode();

    /**
     * Obtains an error text to use for error reporting.
     *
     * <p>This text will also be used as a base for an exception message to generate.
     */
    protected abstract String getErrorText();

    /**
     * Obtains the {@code Message}-specific type attributes for error reporting.
     */
    protected abstract Map<String, Value> getMessageTypeAttribute(Message message);

    /**
     * Defines the way to create an instance of exception basing on the source {@code Message},
     * exception text, and a generated {@code Error}.
     */
    protected abstract E createException(String exceptionMsg, M message, Error error);

    private String formatExceptionMessage() {
        return format("%s. Message class: `%s`. %s",
                      getErrorText(), getMessageClass(), violationsText());
    }

    private Error createError() {
        ValidationError validationError = error();
        R errorCode = getErrorCode();
        String errorType = errorCode.getDescriptorForType()
                                    .getFullName();
        String errorText = errorText();

        Error.Builder error = Error
                .newBuilder()
                .setType(errorType)
                .setCode(errorCode.getNumber())
                .setValidationError(validationError)
                .setMessage(errorText)
                .putAllAttributes(getMessageTypeAttribute(message));
        return error.build();
    }

    private ValidationError error() {
        return ValidationError
                .newBuilder()
                .addAllConstraintViolation(constraintViolations)
                .build();
    }

    private String errorText() {
        String errorTextTemplate = getErrorText();
        String violationsText = violationsText();
        return format("%s %s", errorTextTemplate, violationsText);
    }

    private String violationsText() {
        return ViolationText.ofAll(constraintViolations);
    }

    /**
     * Creates an exception instance for an invalid message which has fields that
     * violate validation constraint(s).
     */
    public E newException() {
        return createException(formatExceptionMessage(), message, createError());
    }
}
