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
import io.spine.tools.compiler.enrichment.mapbuilder.ImageIdListEnrichment;
import io.spine.tools.compiler.enrichment.mapbuilder.ManagerIdEnrichment;
import io.spine.tools.compiler.enrichment.mapbuilder.OwnerIdEnrichment;
import io.spine.tools.compiler.enrichment.mapbuilder.SectionIdListEnrichment;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Ignore
@DisplayName("EnrichmentMapBuilder should build enrichments map for (enrichment_for) option")
class EnrichmentMapBuilderTest {

    private static final String PACKAGE_PREFIX = "spine.tools.compiler.enrichment.mapbuilder.";

    @DisplayName("with multiple targets")
    @Test
    void withMultipleTargets() {
        Map<String, String> expected = ImmutableMap.of(
                "spine.tools.compiler.enrichment.mapbuilder.SectionIdListEnrichment",
                "spine.tools.compiler.enrichment.mapbuilder.OrderCreated," +
                        "spine.tools.compiler.enrichment.mapbuilder.OrderUpdated");
        assertHasEnrichments(SectionIdListEnrichment.getDescriptor(), expected);
    }

    @DisplayName("with a wildcard suffix")
    @Test
    void withWildcardSuffix() {
        Map<String, String> expected = ImmutableMap.of(
                "spine.tools.compiler.enrichment.mapbuilder.ImageIdListEnrichment",
                "spine.tools.compiler.enrichment.mapbuilder.*");
        assertHasEnrichments(ImageIdListEnrichment.getDescriptor(), expected);
    }

    @DisplayName("with multiple targets and complex (by) option")
    @Test
    void withMultipleTargetsAndComplexByOption() {
        Map<String, String> expected = ImmutableMap.of(
                "spine.tools.compiler.enrichment.mapbuilder.OwnerIdEnrichment",
                "spine.tools.compiler.enrichment.mapbuilder.OrderUpdated," +
                        "spine.tools.compiler.enrichment.mapbuilder.SectionUpdated");
        assertHasEnrichments(OwnerIdEnrichment.getDescriptor(), expected);
    }

    @DisplayName("with mixed wildcard and FQN syntax")
    @Test
    void withMixedWildcardAndFqnSyntax() {
        Map<String, String> expected = ImmutableMap.of(
                "spine.tools.compiler.enrichment.mapbuilder.ManagerIdEnrichment",
                "spine.tools.compiler.enrichment.mapbuilder.SectionUpdated," +
                        "spine.tools.compiler.enrichment.mapbuilder.inner.*");
        assertHasEnrichments(ManagerIdEnrichment.getDescriptor(), expected);
    }

    void assertHasEnrichments(Descriptors.Descriptor descriptor,
                              Map<String, String> expected) {
        DescriptorProto descriptorProto = descriptor.toProto();
        Map<String, String> actual = new EnrichmentMapBuilder(PACKAGE_PREFIX)
                .addAll(ImmutableList.of(descriptorProto))
                .toMap();
        assertEquals(expected, actual);
    }
}
