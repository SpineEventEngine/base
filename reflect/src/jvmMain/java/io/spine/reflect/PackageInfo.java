/*
 * Copyright 2023, TeamDev. All rights reserved.
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

import com.google.errorprone.annotations.Immutable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

/**
 * Provides additional run-time information about a Java package.
 */
@Immutable
public final class PackageInfo implements Comparable<PackageInfo> {

    /**
     * Java package which this instance analyzes.
     *
     * @implNote Even though {@code Package} objects are not immutable,
     *           the data we use from them (e.g. {@linkplain Package#getName() name}) is immutable.
     *           That's why it's OK to tell Error Prone that the field is immutable.
     */
    @SuppressWarnings("Immutable") // see implNote
    private final Package value;

    private PackageInfo(Package value) {
        this.value = value;
    }

    /**
     * Obtains an instance for the passed package value.
     */
    public static PackageInfo of(Package value) {
        checkNotNull(value);
        var result = new PackageInfo(value);
        return result;
    }

    /**
     * Obtains an instance with the package of the passed class.
     */
    public static PackageInfo of(Class<?> cls) {
        checkNotNull(cls);
        var result = new PackageInfo(cls.getPackage());
        return result;
    }

    /**
     * Finds an annotation of the specified type, set directly to the package or via the package
     * nesting hierarchy.
     *
     * <p>Tries to obtain the annotation if it presents directly in this package.
     * If not, tries to obtain the annotation from the packages in which this package
     * is nested, staring from innermost.
     *
     * <p>If none of the packages has the required annotation, returns {@link Optional#empty()}.
     *
     * @param annotationClass
     *         the class of the annotations
     * @param <A>
     *         the type of the annotation to query
     * @return annotation or {@link Optional#empty()} if not found
     */
    public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationClass) {
        checkNotNull(annotationClass);
        var result = directAnnotation(annotationClass);
        if (result.isPresent()) {
            return result;
        }

        List<PackageInfo> goingUp = new ArrayList<>(parents());
        goingUp.sort(reverseOrder());
        for (var parent : goingUp) {
            var ofParent = parent.directAnnotation(annotationClass);
            if (ofParent.isPresent()) {
                return ofParent;
            }
        }
        return Optional.empty();
    }

    /**
     * Obtains an annotation of the specified type if it's <em>directly</em>
     * present in the package.
     *
     * @param annotationClass
     *         the class of the annotations
     * @param <A>
     *         the type of the annotation to query
     * @return the annotation or {@link Optional#empty()} if not found
     * @see #findAnnotation(Class)
     */
    public <A extends Annotation> Optional<A> directAnnotation(Class<A> annotationClass) {
        checkNotNull(annotationClass);
        var annotation = value.getAnnotation(annotationClass);
        var result = Optional.ofNullable(annotation);
        return result;
    }

    /**
     * Obtains an annotation of the specified type if it's <em>directly</em>
     * present in the package.
     * @deprecated please use {@link #directAnnotation(Class)} instead.
     */
    @Deprecated(forRemoval = true)
    public <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass) {
        return directAnnotation(annotationClass);
    }

    /**
     * Obtains parents of this package in the alphabetical order.
     */
    private List<PackageInfo> parents() {
        var pack = Package.getPackages();
        var packageName = getName();
        var parentList = Arrays.stream(pack)
                .filter((p) -> {
                    var parentName = p.getName();
                    return packageName.startsWith(parentName) &&
                            !packageName.equals(parentName);
                })
                .sorted(comparing(Package::getName))
                .collect(toList());
        var result = parentList.stream()
                .map(PackageInfo::of)
                .collect(toList());
        return result;
    }

    /**
     * Obtains the value stored in the node.
     */
    public Package getValue() {
        return value;
    }

    /**
     * Returns {@code true} if the enclosed package value is equal to the passed instance.
     */
    boolean isAbout(Package p) {
        checkNotNull(p);
        var result = value.equals(p);
        return result;
    }

    public String getName() {
        return value.getName();
    }

    /**
     * Returns the name of the package.
     */
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PackageInfo)) {
            return false;
        }
        var node = (PackageInfo) o;
        return Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(PackageInfo o) {
        var thisName = value.getName();
        var otherName = o.getValue().getName();
        return thisName.compareTo(otherName);
    }
}
