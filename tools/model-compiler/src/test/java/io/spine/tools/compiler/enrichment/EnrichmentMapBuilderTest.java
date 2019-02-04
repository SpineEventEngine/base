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

package io.spine.tools.compiler.enrichment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.Descriptors;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Ignore
@DisplayName("EnrichmentMapBuilder should")
class EnrichmentMapBuilderTest {

    private static final String PACKAGE_PREFIX = "io.spine.tools.compiler.enrichment.";

    @DisplayName("build enrichments map for")
    @Nested
    class BuildEnrichmentsMapFor {

        @DisplayName("(enrichment_for) option with multiple targets")
        @Test
        void enrichmentForOptionWithMultipleTargets() {
            Map<String, String> expected = ImmutableMap.of(
                    "io.spine.tools.compiler.enrichment.SectionIdListEnrichment",
                    "spine.tools.compiler.enrichment.OrderCreated,spine.tools.compiler.enrichment.OrderUpdated");
            assertHasEnrichments(SectionIdListEnrichment.getDescriptor(), expected);
        }

        @DisplayName("(by) option")
        @Test
        void byOption() {
            Map<String, String> expected = ImmutableMap.of(
                    "io.spine.tools.compiler.enrichment.UserIdEnrichment",
                    "spine.tools.compiler.enrichment.OrderUpdated");
            assertHasEnrichments(UserIdEnrichment.getDescriptor(), expected);
        }

        @DisplayName("nested into event enrichment with")
        @Nested
        class NestedEnrichment {

            @DisplayName("FQN (by) option")
            @Test
            void fqnByOption() {
                Map<String, String> expected = ImmutableMap.of(
                        "io.spine.tools.compiler.enrichment.OrderCanceled.Enrichment",
                        "io.spine.tools.compiler.enrichment.OrderCanceled"
                );
                assertHasEnrichments(OrderCanceled.getDescriptor(), expected);
            }

            @DisplayName("(enrichment_for) and (by) shortcut options")
            @Test
            void enrichmentForAndByOptions() {
                Map<String, String> expected = ImmutableMap.of(
                        "io.spine.tools.compiler.enrichment.OrderRefunded.Enrichment",
                        "io.spine.tools.compiler.enrichment.OrderRefunded"
                );
                assertHasEnrichments(OrderRefunded.getDescriptor(), expected);
            }
        }
    }

    void assertHasEnrichments(Descriptors.Descriptor descriptor, Map<String, String> expected) {
        assertHasEnrichments(PACKAGE_PREFIX, descriptor, expected);
    }

    void assertHasEnrichments(String packagePrefix,
                              Descriptors.Descriptor descriptor,
                              Map<String, String> expected) {
        DescriptorProto descriptorProto = descriptor.toProto();
        Map<String, String> actual = new EnrichmentMapBuilder(packagePrefix)
                .addAll(ImmutableList.of(descriptorProto))
                .toMap();
        assertEquals(expected, actual);
    }
}
