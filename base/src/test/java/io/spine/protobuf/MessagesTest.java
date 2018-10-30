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
package io.spine.protobuf;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.test.messages.MessageWithStringValue;
import io.spine.testing.TestValues;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.protobuf.Messages.builderFor;
import static io.spine.protobuf.Messages.ensureMessage;
import static io.spine.protobuf.TypeConverter.toAny;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Messages utility class should")
class MessagesTest extends UtilityClassTest<Messages> {

    MessagesTest() {
        super(Messages.class);
    }

    @Test
    @DisplayName("return the same any from toAny")
    void return_the_same_any_from_toAny() {
        Any any = toAny(getClass().getSimpleName());
        assertSame(any, AnyPacker.pack(any));
    }

    @Test
    @DisplayName("pack to Any")
    void pack_to_Any() {
        Timestamp timestamp = Time.getCurrentTime();
        assertEquals(timestamp, unpack(AnyPacker.pack(timestamp)));
    }

    @Test
    @DisplayName("return builder for the message")
    void return_builder_for_the_message() {
        Message.Builder messageBuilder = builderFor(MessageWithStringValue.class);
        assertNotNull(messageBuilder);
        assertEquals(MessageWithStringValue.class,
                     messageBuilder.build()
                                   .getClass());
    }

    @Test
    @DisplayName("throw when try to get builder for a not generated message")
    void throw_exception_when_try_to_get_builder_for_not_the_generated_message() {
        assertThrows(IllegalArgumentException.class,
                     () -> builderFor(Message.class));
    }

    @Test
    @DisplayName("return true when message is checked")
    void return_true_when_message_is_checked() {
        assertTrue(Messages.isMessage(MessageWithStringValue.class));
    }

    @Test
    @DisplayName("return false when not message is checked")
    void return_false_when_not_message_is_checked() {
        assertFalse(Messages.isMessage(getClass()));
    }

    @Test
    @DisplayName("ensure Message")
    void ensure_Message() {
        StringValue value = TestValues.newUuidValue();
        assertEquals(value, ensureMessage(AnyPacker.pack(value)));
        assertSame(value, ensureMessage(value));
    }
}
