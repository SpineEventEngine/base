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

import com.google.common.truth.BooleanSubject;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.test.code.enrichment.fieldref.FdtAccountSuspended;
import io.spine.test.code.enrichment.fieldref.FdtDuplicateContextRef;
import io.spine.test.code.enrichment.fieldref.FdtUserCreated;
import io.spine.test.code.enrichment.fieldref.FdtUserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FieldDef should")
class FieldDefTest {

    private FieldDef fieldDef;
    private FieldDescriptor descriptor;

    @BeforeEach
    void setUp() {
        descriptor = FdtUserProfile.getDescriptor()
                                   .findFieldByName("given_name");
        checkNotNull(descriptor);
        fieldDef = new FieldDef(descriptor);
    }

    @Test
    @DisplayName("provide the descriptor of the field")
    void descr() {
        assertThat(fieldDef.descriptor())
                .isEqualTo(descriptor);
    }

    @Nested
    @DisplayName("test if a type matches")
    class TypeMatch {

        @Test
        @DisplayName("positively if both type and field reference match")
        void matchesType() {
            assertMatches(FdtUserCreated.getDescriptor())
                    .isTrue();
        }

        @Test
        @DisplayName("negatively if type matches, but field reference does not")
        void fieldDoesNotMatch() {
            assertMatches(FdtAccountSuspended.getDescriptor())
                .isFalse();
        }

        private BooleanSubject assertMatches(Descriptor type) {
            return assertThat(fieldDef.matchesType(type));
        }
    }

    @Nested
    @DisplayName("prohibit")
    class InvalidRefCombinations {

        @Test
        @DisplayName("two or more `context` references in one field")
        void twoContext() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> {
                        FieldDescriptor field =
                                FdtDuplicateContextRef.getDescriptor()
                                                      .findFieldByName("context_info");
                        new FieldDef(field);
                    }
            );
        }
    }
}
