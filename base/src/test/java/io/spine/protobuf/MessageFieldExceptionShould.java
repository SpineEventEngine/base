/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
import io.spine.test.Tests;
import org.junit.Test;

import static io.spine.test.TestValues.newUuidValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowableNotThrown")
public class MessageFieldExceptionShould {

    @Test
    public void construct_instance_with_formatted_message() {
        final String param1 = "Букварь";
        final String param2 = "blue";
        final String param3 = String.valueOf(3);
        final StringValue protobufMessage = newUuidValue();
        final MessageFieldException exception =
                new MessageFieldException(protobufMessage,
                                          "Reading log is: %s %s %s",
                                          param1, param2, param3);

        assertEquals(protobufMessage, exception.getProtobufMessage());
        final String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains(param1));
        assertTrue(exceptionMessage.contains(param2));
        assertTrue(exceptionMessage.contains(param3));
    }

    @Test
    public void contains_instance_without_text() {
        final Timestamp protobufMessage = Time.getCurrentTime();
        final MessageFieldException exception = new MessageFieldException(protobufMessage);

        assertEquals(protobufMessage, exception.getProtobufMessage());
        assertTrue(exception.getMessage().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void allow_null_params() {
        new MessageFieldException(Empty.getDefaultInstance(), Tests.<String>nullRef());
    }
}
