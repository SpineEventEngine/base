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
import com.google.common.collect.ImmutableSet;
import com.google.common.truth.IterableSubject;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.code.proto.enrichment.packages.OpCommonAncestorEnrichment;
import io.spine.code.proto.enrichment.packages.PaCommonAncestorEnrichment;
import io.spine.code.proto.enrichment.packages.PaFqnIdEnrichment;
import io.spine.code.proto.enrichment.packages.PaPlainIdEnrichment;
import io.spine.code.proto.enrichment.packages.PbCommonAncestorEnrichment;
import io.spine.code.proto.enrichment.packages.PbFqnIdEnrichment;
import io.spine.code.proto.enrichment.packages.PbPlainIdEnrichment;
import io.spine.code.proto.enrichment.packages.SpFqnIdEnrichment;
import io.spine.code.proto.enrichment.packages.SpPlainIdEnrichment;
import io.spine.code.proto.ref.UserId;
import io.spine.test.code.enrichment.fieldref.AssertionFailed;
import io.spine.test.code.enrichment.fieldref.OverlySpecificStacktrace;
import io.spine.test.code.enrichment.fieldref.Stacktrace;
import io.spine.test.code.enrichment.fieldref.TestFailed;
import io.spine.test.code.enrichment.fieldref.WildcardStacktrace;
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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
         * @apiNote If this test breaks with {@code unexpected (<number>)} output in
         *         console,
         *         which is followed with the enumeration of unexpected types like this:
         *
         * <pre>
         *unexpected (2): class io.spine.test.code.enrichment.type.user.sharing.EttSharingRequestSent,
         * class io.spine.test.code.enrichment.type.user.sharing.EttSharingRequestApproved
         *</pre>
         *
         *         please make sure these types are added in the verification list in the body of
         *         this
         *         method. Do not relax the checking condition.
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
    }

    @Nested
    @DisplayName("when referencing types directly")
    class DirectReference {

        private final Class<UserId> expected = UserId.class;

        @Nested
        @DisplayName("by their FQN")
        @TestInstance(TestInstance.Lifecycle.PER_CLASS) /* To allow non-static `@MethodSource`. */
        class Fqns {

            @ParameterizedTest
            @MethodSource("fqnsOfEnrichments")
            @DisplayName("allow to reference any messages")
            void allowsAlways(Class<? extends Message> enrichmentClass) {
                assertSourceClassesOf(enrichmentClass).containsExactly(expected);
            }

            private Set<Arguments> fqnsOfEnrichments() {
                return ImmutableSet.of(
                        arguments(PbFqnIdEnrichment.class),
                        arguments(SpFqnIdEnrichment.class),
                        arguments(PaFqnIdEnrichment.class)
                );
            }
        }

        @Nested
        @DisplayName("by just the message name")
        @TestInstance(TestInstance.Lifecycle.PER_CLASS) /* To allow non-static `@MethodSource`. */
        class MessageName {

            @Test
            @DisplayName("allow if referenced  from the same package")
            void allowsShortReferences() {
                assertSourceClassesOf(SpPlainIdEnrichment.class).containsExactly(expected);
            }

            @ParameterizedTest
            @MethodSource("messageNamesOfEnrichments")
            @DisplayName("disallow if referenced from outside")
            void disallowsForOutsidePackages(Class<? extends Message> enrichmentClass) {
                assertSourceClassesOf(enrichmentClass).isEmpty();
            }

            private Set<Arguments> messageNamesOfEnrichments() {
                return ImmutableSet.of(
                        arguments(PbPlainIdEnrichment.class),
                        arguments(PaPlainIdEnrichment.class)
                );
            }
        }

        @Nested
        @DisplayName("by non-full package definition")
        class ExplicitPackageDifference {

            @Test
            @DisplayName("allow referencing from child package in the same hierarchy")
            void allowsFromChildPackage() {
                assertSourceClassesOf(PbCommonAncestorEnrichment.class).containsExactly(expected);
            }

            @Test
            @DisplayName("allow referencing from parent package in the same hierarchy")
            void allowsFromParentPackage() {
                assertSourceClassesOf(PaCommonAncestorEnrichment.class).containsExactly(expected);
            }

            @Test
            @DisplayName("disallow referencing from an outside package hierarchy")
            void allowsFromOutside() {
                assertSourceClassesOf(OpCommonAncestorEnrichment.class).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("when referencing types by package")
    class PackageReference {

        @Test
        @DisplayName("allow to reference all messages in the containing package by short package name")
        void allowsPackageWideShortReferences() {
            assertEnriched(Stacktrace.class);
        }

        @Test
        @DisplayName("allow to reference all messages in the containing package with a short name")
        void allowsOverlyExplicitPackageReferences() {
            assertEnriched(OverlySpecificStacktrace.class);
        }

        @Test
        @DisplayName("throw upon a package reference containing a wildcard only")
        void throwsOnWildcard() {
            assertThrows(IllegalArgumentException.class,
                         () -> assertSourceClassesOf(WildcardStacktrace.class));
        }

        @Test
        @DisplayName("disallow to reference outside packages not using its full name")
        void disallowToShorthandReferenceOutsidePackages() {
            assertSourceClassesOf(OutOfReachStacktrace.class).isEmpty();
        }

        @Test
        @DisplayName("allow FQN package references from outside packages")
        void allowFqnPackageReference() {
            assertEnriched(FqnStacktrace.class);
        }

        @Test
        @DisplayName("allow child package shorthand references")
        void allowShorthandChildReference(){
            assertEnriched(ShorthandStacktrace.class);
        }

        @Test
        @DisplayName("throw on a non-existing package reference")
        void throwOnNonExisting() {
            assertThrows(IllegalArgumentException.class,
                         () -> assertSourceClassesOf(ProjectDetails.class));
        }

        private void assertEnriched(Class<? extends Message> cls){
            assertSourceClassesOf(cls).containsExactly(AssertionFailed.class,
                                                       TestFailed.class);
        }
    }

    /**
     * Creates an iterable subject for source types of the passed enrichment class.
     */
    private static IterableSubject assertSourceClassesOf(Class<? extends Message> cls) {
        EnrichmentType et = new EnrichmentType(descriptorOf(cls));
        ImmutableList<? extends Class<? extends Message>> sourceClasses = et.sourceClasses();
        return assertThat(sourceClasses);
    }

    private static Descriptor descriptorOf(Class<? extends Message> cls) {
        return TypeName.of(cls)
                       .messageDescriptor();
    }
}
