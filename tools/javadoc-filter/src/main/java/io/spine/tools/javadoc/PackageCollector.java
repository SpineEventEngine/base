/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.javadoc;

import com.google.errorprone.annotations.Immutable;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Collects {@linkplain PackageDoc}s that match the specified {@linkplain AnnotationCheck}.
 */
@Immutable
final class PackageCollector {

    private final AnnotationCheck<?> annotationCheck;

    PackageCollector(AnnotationCheck<?> annotationCheck) {
        this.annotationCheck = annotationCheck;
    }

    /**
     * Collects {@linkplain PackageDoc package documentation}s from
     * the {@linkplain RootDoc#specifiedPackages() packages} of the passed documentation root,
     * and packages of the {@linkplain RootDoc#specifiedClasses() classes} of this root.
     *
     * @return a set sorted by the package {@linkplain PackageDoc#name() names}
     */
    Set<PackageDoc> collect(RootDoc root) {
        Set<PackageDoc> packages = newSortedSet();
        packages.addAll(collect(root.specifiedPackages()));
        packages.addAll(collect(root.specifiedClasses()));
        return packages;
    }

    /**
     * Creates a new sorted set for storing the gathered data.
     */
    private static Set<PackageDoc> newSortedSet() {
        return new TreeSet<>(new PackageDocComparator());
    }

    /**
     * Creates a new sorted set initialized with the passed content.
     */
    private static Set<PackageDoc> newSortedSet(Set<PackageDoc> content) {
        Set<PackageDoc> result = newSortedSet();
        result.addAll(content);
        return result;
    }

    private Set<PackageDoc> collect(ClassDoc[] classes) {
        Set<PackageDoc> packages = packagesOf(classes);
        Set<PackageDoc> allCollected = newSortedSet(packages);
        for (ClassDoc cls : classes) {
            if (isSubpackage(cls.containingPackage(), packages)) {
                allCollected.add(cls.containingPackage());
            }
        }
        return allCollected;
    }

    private Collection<PackageDoc> collect(PackageDoc[] packages) {
        Set<PackageDoc> basePackages = packagesOf(packages);
        Set<PackageDoc> allCollected = newSortedSet(basePackages);
        for (PackageDoc pckg : packages) {
            if (isSubpackage(pckg, basePackages)) {
                allCollected.add(pckg);
            }
        }
        return allCollected;
    }

    private Set<PackageDoc> packagesOf(PackageDoc[] packages) {
        Set<PackageDoc> result = newSortedSet();
        for (PackageDoc packageDoc : packages) {
            if (annotationCheck.test(packageDoc)) {
                result.add(packageDoc);
            }
        }
        return result;
    }

    private Set<PackageDoc> packagesOf(ClassDoc[] classes) {
        Set<PackageDoc> result = newSortedSet();
        for (ClassDoc cls : classes) {
            if (annotationCheck.test(cls.containingPackage())) {
                result.add(cls.containingPackage());
            }
        }
        return result;
    }

    private static boolean isSubpackage(PackageDoc target, Iterable<PackageDoc> packages) {
        for (PackageDoc pckg : packages) {
            if (target.name().startsWith(pckg.name())) {
                return true;
            }
        }
        return false;
    }

    private static class PackageDocComparator implements Comparator<PackageDoc>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(PackageDoc o1, PackageDoc o2) {
            return o1.name()
                     .compareTo(o2.name());
        }
    }
}
