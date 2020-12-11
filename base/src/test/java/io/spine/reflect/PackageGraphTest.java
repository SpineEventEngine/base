/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PackageGraph should")
class PackageGraphTest {

    private final PackageGraph graph = PackageGraph.newInstance();

    // Test values representing well-known Java packages.
    private final PackageInfo javaLang = PackageInfo.of(String.class);
    private final PackageInfo javaUtil = PackageInfo.of(Collection.class.getPackage());
    private final PackageInfo javaUtilConcurrent = PackageInfo.of(Callable.class.getPackage());

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nullCheck() {
        new NullPointerTester().testAllPublicStaticMethods(PackageGraph.class);
    }

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

    @DisplayName("Implement Graph interface")
    @Nested
    class GraphApi {

        @Test
        @DisplayName("returning edges")
        void edges() {
            assertFalse(graph.edges()
                             .isEmpty());
        }

        @Test
        @DisplayName("be directed")
        void directed() {
            assertTrue(graph.isDirected());
        }

        @Test
        @DisplayName("not allowing self loops")
        void selfLoops() {
            assertFalse(graph.allowsSelfLoops());
        }

        @Test
        @DisplayName("having natural node order")
        void naturalOrder() {
            assertEquals(ElementOrder.<PackageInfo>natural(), graph.nodeOrder());
        }

        @Test
        @DisplayName("returning adjacent nodes")
        void adjacentNodes() {
            Set<PackageInfo> nodes = graph.adjacentNodes(javaUtil);
            assertContainsPackageOf(nodes, Callable.class);
            assertContainsPackageOf(nodes, Function.class);
            assertContainsPackageOf(nodes, Logger.class);
        }

        @Test
        @DisplayName("obtaining predecessors")
        void predecessors() {
            Set<PackageInfo> predecessors = graph.predecessors(javaUtilConcurrent);

            assertContainsPackageOf(predecessors, AtomicBoolean.class);
            assertContainsPackageOf(predecessors, Lock.class);
        }

        @Test
        @DisplayName("obtaining successors")
        void successors() {
            Set<PackageInfo> successors = graph.successors(javaUtilConcurrent);

            assertEquals(1, successors.size());
            assertContainsPackageOf(successors, Collection.class);
        }

        private void assertContainsPackageOf(Set<PackageInfo> nodes, Class<?> cls) {
            assertTrue(nodes.contains(PackageInfo.of(cls)));
        }

        @Test
        @DisplayName("obtaining incident edges")
        void incidentEdges() {
            Set<EndpointPair<PackageInfo>> edges = graph.incidentEdges(javaUtilConcurrent);
            // The primary purpose of these checks is to demonstrate how the edges work.
            // They do not test our code since we simply redirect to Guava's Graph.
            for (EndpointPair<PackageInfo> edge : edges) {
                boolean isSource = edge.source()
                                       .equals(javaUtilConcurrent);
                boolean isTarget = edge.target()
                                       .equals(javaUtilConcurrent);
                assertTrue(isSource || isTarget);
            }
        }

        @Nested
        @DisplayName("Return degree")
        class Degree {

            @Test
            @DisplayName("total")
            void degree() {
                assertNotZero(() -> graph.degree(javaUtilConcurrent));
            }

            @Test
            @DisplayName("incoming")
            void inDegree() {
                assertNotZero(() -> graph.inDegree(javaUtilConcurrent));
            }

            @Test
            @DisplayName("outgoing")
            void outDegree() {
                assertNotZero(() -> graph.outDegree(javaUtilConcurrent));
            }

            /**
             * Asserts that the value iz not zero.
             *
             * @implNote We do not compare with exact value to prevent breaking the test when
             * Java gets more sub-packages under {@link java.util.concurrent}.
             */
            private void assertNotZero(IntSupplier s) {
                assertNotEquals(0, s.getAsInt());
            }
        }

        @Nested
        @DisplayName("Check edge")
        class Edge {

            @Test
            @DisplayName("connected")
            void connected() {
                assertTrue(graph.hasEdgeConnecting(javaUtilConcurrent, javaUtil));
            }

            @Test
            @DisplayName("directed")
            void directed() {
                assertFalse(graph.hasEdgeConnecting(javaUtil, javaUtilConcurrent));
            }

            @Test
            @DisplayName("not connected")
            void notConnected() {
                // Indirect connection.
                assertFalse(graph.hasEdgeConnecting(PackageInfo.of(Lock.class), javaUtil));
                // Another branch.
                assertFalse(graph.hasEdgeConnecting(javaLang, javaUtil));
            }
        }
    }

    /**
     * Debug utility for printing nodes and edges.
     */
    @SuppressWarnings({"unused", "UseOfSystemOutOrSystemErr"}) // see Javadoc
    static class Print {

        /** Prevents instantiation of this utility class. */
        private Print() {
        }

        static void nodes(Collection<PackageInfo> nodes) {
            for (PackageInfo node : nodes) {
                System.out.println(node);
            }
        }

        static void edges(Collection<EndpointPair<PackageInfo>> edges) {
            for (EndpointPair<PackageInfo> edge : edges) {
                System.out.println(edge);
            }
        }
    }
}
