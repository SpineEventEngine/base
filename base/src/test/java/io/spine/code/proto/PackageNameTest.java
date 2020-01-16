/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.proto;

import com.google.common.testing.NullPointerTester;
import com.google.common.truth.BooleanSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("PackageName should")
class PackageNameTest {

    @Test
    void handleNullArgs() {
        new NullPointerTester().testAllPublicStaticMethods(PackageName.class);
    }

    @Test
    @DisplayName("create a new instance by value")
    void newInstance() {
        String packageName = "some.pack.age";
        assertThat(PackageName.of(packageName)
                              .value()).isEqualTo(packageName);
    }

    @Nested
    @DisplayName("verify if the package is inner to a parent package")
    class SubPackage {

        @Test
        @DisplayName("if immediately nested")
        void nested() {
            assertIsInner("spine.code.proto", "spine.code");
        }

        @Test
        @DisplayName("if nested deeper")
        void deepNesting() {
            assertIsInner("spine.code.proto.ref", "spine");
        }

        @Test
        @DisplayName("returning `false` if not")
        void notInner() {
            assertInner("spine.code.proto", "spine.code.java")
                    .isFalse();
        }

        void assertIsInner(String inner, String outer) {
            BooleanSubject assertInner = assertInner(inner, outer);
            assertInner.isTrue();
        }

        private BooleanSubject assertInner(String inner, String outer) {
            PackageName innerPackage = PackageName.of(inner);
            PackageName outerPackage = PackageName.of(outer);
            return assertThat(innerPackage.isInnerOf(outerPackage));
        }
    }
}
