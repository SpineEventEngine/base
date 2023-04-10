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

package io.spine.logging

/**
 * Provides additional run-time information about a Java package.
 */
internal class JvmPackage(val value: Package) : Comparable<JvmPackage> {

    val name: String = value.name

    /**
     * Returns the name of the package.
     */
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is JvmPackage) {
            return false
        }
        val node: JvmPackage = other
        return value == node.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun compareTo(other: JvmPackage): Int = value.name.compareTo(other.value.name)
}

/**
 * Finds an annotation of the specified type, set directly to the package or
 * via the package nesting hierarchy.
 *
 * Tries to obtain the annotation if it presents directly in this package.
 * If not, tries to obtain the annotation from the packages in which this package
 * is nested, staring from innermost.

 * If none of the packages has the required annotation, returns null.
 */
internal fun <A : Annotation?> JvmPackage.findAnnotation(annotationClass: Class<A>): A? {
    val direct = directAnnotation(annotationClass)
    if (direct != null) {
        return direct
    }
    for (parent in parents()) {
        val ofParent = parent.directAnnotation(annotationClass)
        if (ofParent != null) {
            return ofParent
        }
    }
    return null
}

/**
 * Obtains parents of this package in the reverse alphabetical order.
 */
private fun JvmPackage.parents(): List<JvmPackage> {
    val allPackages = Package.getPackages()
    val packageName = name
    val parentList = allPackages.filter {
        val parentName = it.name
        packageName.startsWith(parentName) && packageName != parentName
    }
    val sortedParentList = parentList
        .sortedWith(compareBy { it.name })
        .reversed()
    return sortedParentList.map { JvmPackage(it) }
}

/**
 * Obtains an annotation of the specified type if it's *directly*
 * present in the package.
 */
private fun <A : Annotation?> JvmPackage.directAnnotation(annotationClass: Class<A>): A? {
    return value.getAnnotation(annotationClass)
}
