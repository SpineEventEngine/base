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

package io.spine.code.proto.ref;

import com.google.common.truth.OptionalSubject;
import com.google.common.truth.Truth8;
import com.google.protobuf.Any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("InPackage should")
class InPackageTest {


    @Nested
    @DisplayName("parse wildcard package reference")
    class ParseValue {

        @Test
        @DisplayName("returning value for delimited package name")
        void delimited() {
            assertParses("spine.test.*");
        }

        @Test
        @DisplayName("returning value for a simple package name")
        void simple() {
            assertParses("spine.*");
        }

        @Test
        @DisplayName("rejecting value without a wildcard")
        void noWildcard() {
            assertNotParses("spine.test");
        }

        void assertParses(String value) {
            Optional<TypeRef> typeRef = InPackage.parse(value);
            OptionalSubject subject = Truth8.assertThat(typeRef);
            subject.isPresent();
            @SuppressWarnings("OptionalGetWithoutIsPresent") // checked via isPresent();
            String reference = typeRef.get()
                                      .value();
            assertThat(reference)
                    .isEqualTo(value);
        }

        void assertNotParses(String value) {
            Truth8.assertThat(InPackage.parse(value))
                  .isEmpty();
        }
    }

    @Test
    @DisplayName("tell if a message belongs to the referenced package directly")
    void byDescriptor() {
        @SuppressWarnings("OptionalGetWithoutIsPresent") // We're sure it will work.
        TypeRef ref = InPackage.parse("google.protobuf.*")
                               .get();
        assertThat(ref.test(Any.getDescriptor()))
                .isTrue();
    }
}
