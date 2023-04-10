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

import io.spine.logging.LoggingDomain.Companion.noOp
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Obtains a [LoggingDomain] for a Java- or Kotlin class.
 */
internal object LoggingDomainClassValue: ClassValue<LoggingDomain>() {

    internal fun get(cls: KClass<*>) = get(cls.java)

    override fun computeValue(javaClass: Class<*>): LoggingDomain {
        val domain = javaClass.kotlin.findWithNesting<LoggingDomain>()
        if (domain != null) {
            return domain
        }
        val jvmLoggingDomain = javaClass.kotlin.findWithNesting<JvmLoggingDomain>()
        if (jvmLoggingDomain != null) {
            return jvmLoggingDomain.toLoggingDomain()
        }

        val currentPackage = javaClass.`package`
        val jvmDomain = JvmPackage(currentPackage).findAnnotation(JvmLoggingDomain::class.java)
        return jvmDomain?.toLoggingDomain() ?: noOp
    }
}

/**
 * Attempts to find the annotation of type [T] in this [KClass] or enclosing classes.
 */
private inline fun <reified T: Annotation> KClass<*>.findWithNesting(): T? {
    var found: T? = findAnnotation<T>()
    if (found != null) {
        return found
    }
    // Find annotation in enclosing classes, if any.
    var enclosingClass = java.enclosingClass
    while (enclosingClass != null) {
        found = enclosingClass.kotlin.findAnnotation<T>()
        if (found != null) {
            return found
        }
        enclosingClass = enclosingClass.enclosingClass
    }
    return null
}

/**
 * Converts this [JvmLoggingDomain] instance to [LoggingDomain] instance.
 */
private fun JvmLoggingDomain.toLoggingDomain(): LoggingDomain {
    return LoggingDomain(value)
}
