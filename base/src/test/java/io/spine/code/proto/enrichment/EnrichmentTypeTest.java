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

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.IterableSubject;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;
import io.spine.code.proto.MessageType;
import io.spine.test.code.enrichment.type.EttFieldSelection;
import io.spine.test.code.enrichment.type.EttOnAnotherPackageMessage;
import io.spine.test.code.enrichment.type.EttOnDeepPackage;
import io.spine.test.code.enrichment.type.event.EttProjectCreated;
import io.spine.test.code.enrichment.type.user.EttUserDeletedEvent;
import io.spine.test.code.enrichment.type.user.EttUserLoggedInEvent;
import io.spine.test.code.enrichment.type.user.EttUserLoggedOutEvent;
import io.spine.test.code.enrichment.type.user.EttUserMentionedEvent;
import io.spine.test.code.enrichment.type.user.permission.EttPermissionGrantedEvent;
import io.spine.test.code.enrichment.type.user.permission.EttPermissionRevokedEvent;
import io.spine.type.TypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.truth.Truth.assertThat;

@DisplayName("EnrichmentType should")
class EnrichmentTypeTest {

    @Nested
    @DisplayName("tell source message types")
    class SourceMessageTypes {

        @Test
        @DisplayName("for a message in another package")
        void oneClass() {
             assertSourceClassesOf(EttOnAnotherPackageMessage.class)
                     .containsExactly(EttProjectCreated.class);
        }

        @Test
        @DisplayName("for messages in a package filtering by field name")
        void filterByField() {
            IterableSubject assertThat = assertSourceClassesOf(EttFieldSelection.class);
            assertThat.containsExactly(EttPermissionGrantedEvent.class);

            assertThat.doesNotContain(EttPermissionRevokedEvent.class); /* because it
                does not have the `granter_uid` field referenced in the enrichment field
                `(by)` option. */
        }

        @Test
        @DisplayName("for all messages inside nested packages")
        void deepPackage() {
            IterableSubject assertThat = assertSourceClassesOf(EttOnDeepPackage.class);
            assertThat.containsExactly(EttUserLoggedInEvent.class,
                                       EttUserMentionedEvent.class,
                                       EttUserLoggedOutEvent.class,

                                       EttPermissionGrantedEvent.class,
                                       EttPermissionRevokedEvent.class);

            assertThat.doesNotContain(EttUserDeletedEvent.class); /* because its field name
                does not match the the name referenced in the `(by)` option. */
        }


        IterableSubject assertSourceClassesOf(Class<? extends Message> cls) {
            Descriptor descriptor = TypeName.of(cls)
                                            .getMessageDescriptor();
            EnrichmentType et = EnrichmentType.from(descriptor);
            ImmutableSet<MessageType> sources = et.knownSources();
            ImmutableSet<? extends Class<? extends Message>> sourceClasses =
                    sources.stream()
                           .map(MessageType::javaClass)
                           .collect(toImmutableSet());
            return assertThat(sourceClasses);
        }
    }
}
