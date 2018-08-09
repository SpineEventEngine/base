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

package io.spine.type;

import com.google.common.testing.EqualsTester;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;

@DisplayName("MessageClass should")
class MessageClassTest {

    private static final Class<StringValue> MSG_CLASS = StringValue.class;

    @SuppressWarnings("SerializableInnerClassWithNonSerializableOuterClass")
    @Test
    @DisplayName("provide equality within the class")
    void beEqualWithingClass() {
        new EqualsTester()
                .addEqualityGroup(new TestMessageClass(MSG_CLASS), new TestMessageClass(MSG_CLASS))
                .addEqualityGroup(new MessageClass(MSG_CLASS) {
                    private static final long serialVersionUID = 0L;
                });
    }

    @Test
    void serialize() {
        reserializeAndAssert(new TestMessageClass(MSG_CLASS));
    }

    /**
     * Test environment class.
     */
    private static class TestMessageClass extends MessageClass {

        private static final long serialVersionUID = 0L;

        private TestMessageClass(Class<? extends Message> value) {
            super(value);
        }
    }
}
