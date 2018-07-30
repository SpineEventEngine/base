/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"InnerClassMayBeStatic", "ClassCanBeStatic"})
@DisplayName("PackageGraph should")
class PackageGraphTest {

    @Nested
    @DisplayName("Create instance with packages")
    class Create {

        @Test
        @DisplayName("all known to the caller's ClassLoader")
        void allKnown() {
            PackageGraph graph = PackageGraph.newInstance();
            assertContainsPackageOf(graph, String.class);
            assertContainsPackageOf(graph, Predicate.class);
            assertContainsPackageOf(graph, Test.class);
        }

        @Test
        @DisplayName("having common name prefix")
        void commonPrefix() {
            PackageGraph graph = PackageGraph.containing("org.junit");
            assertNotContainsPackageOf(graph, String.class);
            assertNotContainsPackageOf(graph, Predicate.class);
            assertContainsPackageOf(graph, Test.class);
            assertContainsPackageOf(graph, DisplayName.class);
        }

        @Test
        @DisplayName("accepted by a Filter")
        void filtered() {
            PackageGraph.Filter filter =
                    PackageGraph.newFilter()
                                .exclude("java")
                                .include("java.lang.annotation")
                                .include("java.util.function");

            PackageGraph graph = PackageGraph.matching(filter);

            assertContainsPackageOf(graph, Annotation.class);
            assertContainsPackageOf(graph, Predicate.class);
            assertContainsPackageOf(graph, Test.class);

            assertNotContainsPackageOf(graph, String.class);
            assertNotContainsPackageOf(graph, Collection.class);
        }

        private void assertContainsPackageOf(PackageGraph graph, Class<?> cls) {
            assertTrue(graph.contains(cls.getPackage()));
        }

        private void assertNotContainsPackageOf(PackageGraph graph, Class<?> cls) {
            assertFalse(graph.contains(cls.getPackage()));
        }
    }
}
