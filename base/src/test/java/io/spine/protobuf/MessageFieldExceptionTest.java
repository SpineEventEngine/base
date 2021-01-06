/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.protobuf;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertNpe;
import static io.spine.testing.TestValues.newUuidValue;
import static io.spine.testing.Tests.nullRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`MessageFieldException` should")
class MessageFieldExceptionTest {

    @Test
    @DisplayName("construct an instance with a formatted message")
    void instanceWithFormattedMessage() {
        String param1 = "Букварь";
        String param2 = "blue";
        String param3 = String.valueOf(3);
        StringValue protobufMessage = newUuidValue();
        MessageFieldException exception =
                new MessageFieldException(protobufMessage,
                                          "Reading log is: %s %s %s",
                                          param1, param2, param3);

        assertEquals(protobufMessage, exception.getProtobufMessage());
        String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains(param1));
        assertTrue(exceptionMessage.contains(param2));
        assertTrue(exceptionMessage.contains(param3));
    }

    @Test
    @DisplayName("contain an instance without text")
    void instanceWithoutText() {
        Timestamp protobufMessage = Time.currentTime();
        MessageFieldException exception = new MessageFieldException(protobufMessage);

        assertEquals(protobufMessage, exception.getProtobufMessage());
        assertTrue(exception.getMessage()
                            .isEmpty());
    }

    @Test
    @DisplayName("not allow `null` parameters")
    void prohibitNullParams() {
        assertNpe(() -> new MessageFieldException(Empty.getDefaultInstance(), nullRef()));
    }
}
