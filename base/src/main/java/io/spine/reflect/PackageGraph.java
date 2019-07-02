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

package io.spine.reflect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.Immutable;
import io.spine.repackaged.com.google.common.graph.ElementOrder;
import io.spine.repackaged.com.google.common.graph.EndpointPair;
import io.spine.repackaged.com.google.common.graph.Graph;
import io.spine.repackaged.com.google.common.graph.GraphBuilder;
import io.spine.repackaged.com.google.common.graph.ImmutableGraph;
import io.spine.repackaged.com.google.common.graph.MutableGraph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Provides nesting information for packages {@linkplain Package#getPackages() known}
 * to the caller's {@code ClassLoader}
 *
 * <p>Java does not support true nesting of packages. Package merely forms namespaces for classes.
 * Still, the package naming pattern, storage of source code files in a file system, and
 * directory-like representation of package nesting in modern IDEs suggest that there is some form
 * of nesting, at least in terms of naming.
 *
 * <p>This class implements a {@linkplain Graph#isDirected() directed} {@link Graph} with nodes
 * representing Java packages and edges representing child -&gt; parent relationships:
 *
 * <pre>
 *     java.util.concurrent.atomic -&gt; java.util.concurrent
 *     java.util.concurrent -&gt; java.util
 *     java.util.function -&gt; java.util
 * </pre>
 */
@Immutable
public final class PackageGraph implements Graph<PackageInfo> {

    /** The instance to which we delegate. */
    private final ImmutableGraph<PackageInfo> impl;

    private PackageGraph(Graph<PackageInfo> graph) {
        this.impl = ImmutableGraph.copyOf(graph);
    }

    /**
     * Obtains alphabetically sorted list of packages visible to the caller's {@code ClassLoader}.
     */
    private static ImmutableList<Package> packages() {
        Package[] knownPackages = Package.getPackages();
        Arrays.sort(knownPackages, comparing(Package::getName));
        return ImmutableList.copyOf(knownPackages);
    }

    /**
     * Creates a new instance with the packages visible to the caller's {@code ClassLoader}.
     */
    public static PackageGraph newInstance() {
        return create(packages());
    }

    /**
     * Creates a new filter.
     */
    public static Filter newFilter() {
        return new Filter();
    }

    /**
     * Builds a graph of packages that have the common passed package prefix.
     */
    public static PackageGraph containing(String packagePrefix) {
        checkNotNullOrEmpty(packagePrefix);
        Predicate<Package> predicate = (p) -> p.getName()
                                               .startsWith(packagePrefix);
        return matching(predicate);
    }

    /**
     * Builds a graph of packages that match the passed predicate.
     */
    public static PackageGraph matching(Predicate<Package> predicate) {
        checkNotNull(predicate);
        List<Package> filteredPackages = filterPackages(predicate);
        return create(filteredPackages);
    }

    private static List<Package> filterPackages(Predicate<Package> predicate) {
        List<Package> result = packages().stream()
                                         .filter(predicate)
                                         .collect(toList());
        return result;
    }

    private static PackageGraph create(List<Package> filteredPackages) {
        Graph<PackageInfo> mutableGraph = buildGraph(filteredPackages);
        PackageGraph result = new PackageGraph(mutableGraph);
        return result;
    }

    private static Graph<PackageInfo> buildGraph(List<Package> packages) {
        MutableGraph<PackageInfo> graph = GraphBuilder
                .directed()
                .nodeOrder(ElementOrder.<PackageInfo>natural())
                .build();
        Queue<Package> queue = new ArrayDeque<>(packages);
        Package first = queue.poll();
        while (first != null) {
            final Package current = first;
            Optional<PackageInfo> directParent =
                    graph.nodes()
                         .stream()
                         .filter((node) -> IsDirectParent.of(current)
                                                         .test(node.getValue()))
                         .findFirst();
            PackageInfo newNode = PackageInfo.of(current);
            if (directParent.isPresent()) {
                graph.putEdge(newNode, directParent.get());
            } else {
                graph.addNode(newNode);
            }
            first = queue.poll();
        }
        return graph;
    }

    @VisibleForTesting
    boolean contains(Package p) {
        Optional<PackageInfo> result = nodes().stream()
                                              .filter((node) -> node.isAbout(p))
                                              .findAny();
        return result.isPresent();
    }

    @Override
    public Set<PackageInfo> nodes() {
        return impl.nodes();
    }

    @Override
    public Set<EndpointPair<PackageInfo>> edges() {
        return impl.edges();
    }

    @Override
    public boolean isDirected() {
        return impl.isDirected();
    }

    @Override
    public boolean allowsSelfLoops() {
        return impl.allowsSelfLoops();
    }

    @Override
    public ElementOrder<PackageInfo> nodeOrder() {
        return impl.nodeOrder();
    }

    @Override
    public Set<PackageInfo> adjacentNodes(PackageInfo node) {
        return impl.adjacentNodes(node);
    }

    @Override
    public Set<PackageInfo> predecessors(PackageInfo node) {
        return impl.predecessors(node);
    }

    @Override
    public Set<PackageInfo> successors(PackageInfo node) {
        return impl.successors(node);
    }

    @Override
    public Set<EndpointPair<PackageInfo>> incidentEdges(PackageInfo node) {
        return impl.incidentEdges(node);
    }

    @Override
    public int degree(PackageInfo node) {
        return impl.degree(node);
    }

    @Override
    public int inDegree(PackageInfo node) {
        return impl.inDegree(node);
    }

    @Override
    public int outDegree(PackageInfo node) {
        return impl.outDegree(node);
    }

    @Override
    public boolean hasEdgeConnecting(PackageInfo nodeU, PackageInfo nodeV) {
        return impl.hasEdgeConnecting(nodeU, nodeV);
    }

    @Override
    public boolean hasEdgeConnecting(EndpointPair<PackageInfo> endpoints) {
        return impl.hasEdgeConnecting(endpoints);
    }

    private static void checkNotNullOrEmpty(String packagePrefix) {
        checkNotNull(packagePrefix);
        checkArgument(!packagePrefix.isEmpty(), "Package prefix cannot be empty");
    }

    /**
     * Filters packages by their names.
     *
     * @implNote This class is not thread-safe.
     */
    public static final class Filter implements Predicate<Package> {

        private final Set<String> inclusions = Sets.newHashSet();
        private final Set<String> exclusions = Sets.newHashSet();

        /** Prevents instantiation from outside. */
        private Filter() {
        }

        /**
         * Adds a package prefix for being accepted by the filer.
         */
        public Filter include(String packagePrefix) {
            checkNotNullOrEmpty(packagePrefix);
            inclusions.add(packagePrefix);
            return this;
        }

        /**
         * Makes packages with the passed prefix being rejected by the filter.
         */
        public Filter exclude(String packagePrefix) {
            checkNotNullOrEmpty(packagePrefix);
            exclusions.add(packagePrefix);
            return this;
        }

        /**
         * Filters the passed package by its name.
         *
         * <p>A package is accepted if its name:
         * <ol>
         *     <li>Starts from one of the names added to {@linkplain #include(String)
         *     inclusions}.
         *     <li>Does <em>not</em> start from all the names added to {@linkplain #exclude(String)
         *     exclusions}.
         * </ol>
         */
        @Override
        public boolean test(Package aPackage) {
            String packageName = aPackage.getName();

            if (inclusions.stream()
                          .anyMatch(packageName::startsWith)) {
                return true;
            }

            return exclusions.stream()
                             .noneMatch(packageName::startsWith);
        }
    }
}
