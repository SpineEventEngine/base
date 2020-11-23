/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Signals an error working with a Protobuf message field.
 */
public final class MessageFieldException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    private final GeneratedMessageV3 protobufMessage;

    /**
     * Constructs a {@code MessageFieldException} with the formatted message text.
     *
     * @param protobufMessage
     *        a Protobuf message object working with a field of which caused an error
     * @param errorMessageFormat
     *        a format string for the error message
     * @param params
     *        error message parameters
     */
    public MessageFieldException(Message protobufMessage,
                                 String errorMessageFormat,
                                 Object... params) {
        super(format(checkNotNull(errorMessageFormat), params));
        this.protobufMessage = (GeneratedMessageV3) checkNotNull(protobufMessage);
    }

    /**
     * Constructs a {@code MessageFieldException} without no message text.
     *
     * @param protobufMessage
     *        a Protobuf message object working with a field of which caused an error
     */
    public MessageFieldException(Message protobufMessage) {
        this(protobufMessage, "");
    }

    /**
     * Obtains a Protobuf message working with a field of which caused an error.
     */
    public Message getProtobufMessage() {
        return protobufMessage;
    }
}
