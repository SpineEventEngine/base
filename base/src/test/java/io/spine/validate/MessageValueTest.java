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

package io.spine.validate;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.StringValue;
import com.google.protobuf.Value;
import io.spine.code.proto.FieldContext;
import io.spine.testing.ClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.validate.MessageValue.atTopLevel;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`MessageValue` should")
final class MessageValueTest extends ClassTest<MessageValue> {

    private static final OneofDescriptor VALUE_ONEOF = Value.getDescriptor()
                                                            .getOneofs()
                                                            .get(0);

    MessageValueTest() {
        super(MessageValue.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FieldContext.class, FieldContext.empty());
    }

    @Nested
    @DisplayName("Obtain oneof value")
    class OneofValue {

        @Test
        @DisplayName("using the valid descriptor")
        void withValidDescriptor() {
            boolean boolValue = false;
            Value message = Value
                    .newBuilder()
                    .setBoolValue(boolValue)
                    .build();
            MessageValue value = MessageValue.atTopLevel(message);
            assertOneofValue(value, boolValue);
        }

        @Test
        @DisplayName("and throw IAE if a oneof is not declared in a message")
        void throwOnMissingOneof() {
            StringValue message = StringValue.getDefaultInstance();
            MessageValue value = atTopLevel(message);
            assertIllegalArgument(() -> value.valueOf(VALUE_ONEOF));
        }

        private void assertOneofValue(MessageValue message, Object expectedValue) {
            Optional<FieldValue> optionalValue = message.valueOf(VALUE_ONEOF);
            assertTrue(optionalValue.isPresent());
            FieldValue value = optionalValue.get();
            assertThat(value.singleValue())
                    .isEqualTo(expectedValue);
        }
    }
}
