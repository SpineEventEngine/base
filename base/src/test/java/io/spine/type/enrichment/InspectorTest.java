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

package io.spine.type.enrichment;

import com.google.protobuf.Descriptors.Descriptor;
import io.spine.test.enrichment.type.EttWrongAltFieldRef;
import io.spine.test.enrichment.type.EttWrongDirectTypeRef;
import io.spine.test.enrichment.type.EttWrongFieldRef;
import io.spine.test.enrichment.type.EttWrongPackageRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("EnrichmentType validation should")
class InspectorTest {

    @Nested
    @DisplayName("throw NoMatchingTypeException if")
    class InvalidTypeRef {

        @Test
        @DisplayName("there is no referenced type")
        void invalidDirectType() {
            assertNoTypeMatch(EttWrongDirectTypeRef.getDescriptor());
        }

        @Test
        @DisplayName("there is no type matching the package reference")
        void invalidPackage() {
            assertNoTypeMatch(EttWrongPackageRef.getDescriptor());
        }

        void assertNoTypeMatch(Descriptor descriptor) {
            EnrichmentType et = new EnrichmentType(descriptor);
            assertThrows(NoMatchingTypeException.class, et::validate);
        }
    }

    @Nested
    @DisplayName("throw UnsatisfiedFieldReferenceException")
    class UnsatisfiedFieldRef {
        
        @Test
        @DisplayName("if there is no referenced field in the referenced type")
        void invalidFieldRef() {
            assertInvalidFieldRef(EttWrongFieldRef.getDescriptor());
        }

        @Test
        @DisplayName("for a field reference which is not found in any of the type")
        void invalidAltFieldRef() {
            assertInvalidFieldRef(EttWrongAltFieldRef.getDescriptor());
        }

        void assertInvalidFieldRef(Descriptor descriptor) {
            EnrichmentType et = new EnrichmentType(descriptor);
            assertThrows(UnsatisfiedFieldReferenceException.class, et::validate);
        }
    }
}
