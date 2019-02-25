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

import com.google.common.collect.ImmutableList;
import com.google.common.truth.IterableSubject;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.test.code.enrichment.type.EttAlternativeFieldNames;
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
import io.spine.test.code.enrichment.type.user.sharing.EttSharingRequestApproved;
import io.spine.test.code.enrichment.type.user.sharing.EttSharingRequestSent;
import io.spine.type.MessageType;
import io.spine.type.TypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

        /**
         * Tests that <em>all</em> events from a package are present.
         *
         * @apiNote If this test breaks with {@code unexpected (<number>)} output in console,
         * which is followed with the enumeration of unexpected types like this:
         *
         * <pre>
         * unexpected (2): class io.spine.test.code.enrichment.type.user.sharing.EttSharingRequestSent, class io.spine.test.code.enrichment.type.user.sharing.EttSharingRequestApproved
         * </pre>
         *
         * please make sure these types are added in the verification list in the body of this
         * method. Do not relax the checking condition.
         */
        @Test
        @DisplayName("for all messages inside nested packages, matching the field reference")
        void deepPackage() {
            IterableSubject assertThat = assertSourceClassesOf(EttOnDeepPackage.class);
            assertThat.containsExactly(
                    // Events from the root of the `user` package.
                    EttUserLoggedInEvent.class,
                    EttUserMentionedEvent.class,
                    EttUserLoggedOutEvent.class,

                    // Events from the `permission` sub-package.
                    EttPermissionGrantedEvent.class,
                    EttPermissionRevokedEvent.class,

                    // Events from the `sharing` sub-package.
                    EttSharingRequestSent.class,
                    EttSharingRequestApproved.class
            );

            assertThat.doesNotContain(EttUserDeletedEvent.class); /* because its field name
                does not match the the name referenced in the `(by)` option. */
        }

        @Nested
        @DisplayName("for message types with alternative field names")
        class AltFieldNames {

            private final EnrichmentType et =
                    new EnrichmentType(EttAlternativeFieldNames.getDescriptor());

            private final FieldDescriptor targetField =
                    et.descriptor()
                      .findFieldByName("user_google_uid");

            @Test
            @DisplayName("matching the referenced classes")
            void matchingClasses() {
                assertSourceClassesOf(EttAlternativeFieldNames.class)
                        .containsExactly(EttSharingRequestApproved.class,
                                         EttPermissionGrantedEvent.class);
            }

            @Test
            @DisplayName("matching fields in the source types by the order")
            void altFieldNames() {
                assertSourceMatch(EttSharingRequestApproved.class, "second_user_uid");
                assertSourceMatch(EttPermissionGrantedEvent.class, "user_uid");
            }

            private void assertSourceMatch(Class<? extends Message> source, String fieldName) {
                MessageType srcType = new MessageType(descriptorOf(source));
                FieldMatch match = et.sourceFieldsOf(srcType);
                FieldSource fieldSource = match.sourceOf(targetField);
                assertThat(fieldSource.viaReference())
                        .isFalse();
                assertThat(fieldSource.descriptor())
                        .isEqualTo(srcType.descriptor()
                                          .findFieldByName(fieldName));
            }
        }

        /**
         * Creates an iterable subject for source types of the passed enrichment class.
         */
        IterableSubject assertSourceClassesOf(Class<? extends Message> cls) {
            EnrichmentType et = new EnrichmentType(descriptorOf(cls));
            ImmutableList<? extends Class<? extends Message>> sourceClasses = et.sourceClasses();
            return assertThat(sourceClasses);
        }
    }

    private static Descriptor descriptorOf(Class<? extends Message> cls) {
        return TypeName.of(cls)
                       .messageDescriptor();
    }
}
