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

package io.spine.base;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.base.MessageFile.Predicate;
import io.spine.base.given.MessageFileEventsProto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.base.MessageFile.EVENTS_FILE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MessageFile should")
class MessageFileTest {

    @Nested
    @DisplayName("provide a predicate that")
    class ProvidePredicate {

        private Predicate predicate;

        @BeforeEach
        void setUp() {
            predicate = EVENTS_FILE.predicate();
        }

        @Test
        @DisplayName("accepts the file with matching suffix")
        void acceptingEligibleFile() {
            FileDescriptor descriptor = MessageFileEventsProto.getDescriptor();
            assertTrue(predicate.test(descriptor));
        }

        @Test
        @DisplayName("rejects the file with non-matching suffix")
        void rejectingNonEligibleFile() {
            FileDescriptor descriptor = Any.getDescriptor()
                                           .getFile();
            assertFalse(predicate.test(descriptor));
        }
    }
}
