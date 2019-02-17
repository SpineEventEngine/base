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

package io.spine.code.proto.enrichment;

import com.google.common.truth.BooleanSubject;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.spine.test.code.enrichment.fieldref.BttStubMessageContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.code.proto.enrichment.BuiltIn.ANY;
import static io.spine.code.proto.enrichment.BuiltIn.CONTEXT;

@DisplayName("BuiltIn type references should")
class BuiltInTest {

    @Nested
    @DisplayName("Accept all types in")
    class AcceptAll {

        @Test
        @DisplayName("reference to a message type")
        void self() {
            assertAccepts(ANY, BoolValue.getDescriptor());
            assertAccepts(ANY, Empty.getDescriptor());
        }

        void assertAccepts(Predicate<Descriptor> p, Descriptor message) {
            assertThat(p.test(message))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Support \"context\" reference")
    class ContextRef {

        @Test
        @DisplayName("which rejects types other than implementing `MessageContext`")
        void nonMessageContext() {
            assertRejects(Any.getDescriptor());
            assertRejects(Timestamp.getDescriptor());
        }

        @Test
        @DisplayName("accepting `MessageContext` types")
        void messageContext() {
            assertMatch(BttStubMessageContext.getDescriptor())
                    .isTrue();
        }

        void assertRejects(Descriptor message) {
            assertMatch(message)
                    .isFalse();
        }

        private BooleanSubject assertMatch(Descriptor message) {
            return assertThat(CONTEXT.test(message));
        }
    }
}
