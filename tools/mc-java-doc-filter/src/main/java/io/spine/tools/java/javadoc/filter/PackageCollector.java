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

package io.spine.tools.java.javadoc.filter;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Collects {@linkplain PackageDoc}s that pass {@linkplain AnnotationCheck} checks.
 */
final class PackageCollector {

    private final AnnotationCheck<?> analyst;

    PackageCollector(AnnotationCheck<?> analyst) {
        this.analyst = analyst;
    }

    /**
     * Collects {@linkplain PackageDoc}s from {@linkplain RootDoc#specifiedPackages()}
     * and {@linkplain RootDoc#specifiedClasses()}.
     *
     * @param root the root to collect
     * @return collected {@linkplain PackageDoc}s
     */
    Collection<PackageDoc> collect(RootDoc root) {
        Collection<PackageDoc> packages = new TreeSet<>(new PackageDocComparator());

        packages.addAll(collect(root.specifiedPackages()));
        packages.addAll(collect(root.specifiedClasses()));

        return packages;
    }

    private Collection<PackageDoc> collect(ClassDoc[] forClasses) {
        Collection<PackageDoc> allPackages = getPackages(forClasses);
        Collection<PackageDoc> basePackages = getPackages(forClasses);

        for (ClassDoc classDoc : forClasses) {
            if (isSubpackage(classDoc.containingPackage(), basePackages)) {
                allPackages.add(classDoc.containingPackage());
            }
        }

        return allPackages;
    }

    private Collection<PackageDoc> collect(PackageDoc[] forPackages) {
        Collection<PackageDoc> allPackages = getBasePackages(forPackages);
        Collection<PackageDoc> basePackages = getBasePackages(forPackages);

        for (PackageDoc packageDoc : forPackages) {
            if (isSubpackage(packageDoc, basePackages)) {
                allPackages.add(packageDoc);
            }
        }

        return allPackages;
    }

    private Collection<PackageDoc> getBasePackages(PackageDoc[] forPackages) {
        Collection<PackageDoc> packages = new TreeSet<>(new PackageDocComparator());

        for (PackageDoc packageDoc : forPackages) {
            if (analyst.isAnnotationPresent(packageDoc.annotations())) {
                packages.add(packageDoc);
            }
        }

        return packages;
    }

    private Collection<PackageDoc> getPackages(ClassDoc[] forClasses) {
        Collection<PackageDoc> packages = new TreeSet<>(new PackageDocComparator());

        for (ClassDoc classDoc : forClasses) {
            if (analyst.isAnnotationPresent(classDoc.containingPackage().annotations())) {
                packages.add(classDoc.containingPackage());
            }
        }

        return packages;
    }

    private static boolean isSubpackage(PackageDoc target, Iterable<PackageDoc> packages) {
        for (PackageDoc packageDoc : packages) {
            if (target.name().startsWith(packageDoc.name())) {
                return true;
            }
        }

        return false;
    }

}
