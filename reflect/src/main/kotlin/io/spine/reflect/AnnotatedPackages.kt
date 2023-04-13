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

package io.spine.reflect

/**
 * A collection of packages annotated using the annotation of the type [T].
 */
public class AnnotatedPackages<T: Annotation>(
    /**
     * The class of annotation [T] applied to the packages in this collection.
     */
    public val annotationClass: Class<T>
) {

    /**
     * A list of packages annotated with [annotationClass] listed in reverse
     * alphabetical order of the package names.
     */
    public val packages: List<Package>

    init {
        val allPackages = Package.getPackages()
        packages = allPackages.filter { it.findAnnotation(annotationClass) != null }
            .sortedBy { it.name }
            .reversed()
            .toList()
    }

    /**
     * Tells if the annotated packages has the given package directly or as a sub-package
     * of one in the collection.
     */
    public fun findWithNesting(p: Package): T? {
        val found = packages.firstOrNull {
            p.name.startsWith(it.name)
        }
        return found?.findAnnotation(annotationClass)
    }
}

private fun <T: Annotation> Package.findAnnotation(cls: Class<in T>): T? {
    @Suppress("UNCHECKED_CAST")
    return annotations.firstOrNull { cls.isInstance(it) } as T?
}
