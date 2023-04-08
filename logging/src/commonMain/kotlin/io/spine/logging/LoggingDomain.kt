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

import io.spine.logging.LoggingDomain.Companion.get
import kotlin.reflect.KClass

@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
@Retention
public annotation class LoggingDomain(public val name: String) {

    public companion object {

        /**
         * A no-op instance of `LoggingDomain` returned for classes belonging to
         * classes or files without an associated logging domain.
         */
        internal val noOp: LoggingDomain = LoggingDomain("")

        /**
         * Maps a package name to the associated [LoggingDomain].
         */
        private val entries: MutableMap<String, LoggingDomain> =
            mutableMapOf<String, LoggingDomain>().toSortedMap(compareByDescending { it })

        @JvmStatic
        public fun put(packageName: String, domain: LoggingDomain): LoggingDomain? =
            entries.put(packageName, domain)

        /**
         * Obtains a logging domain for the class. If a domain is not specifically set, a no-op
         * instance with an empty name will be returned.
         */
        @JvmStatic
        public fun get(cls: KClass<*>): LoggingDomain {
            val packageName = cls.packageName ?: return noOp
            val found = entries.keys.firstOrNull { packageName.startsWith(it) }
            return if (found != null) {
                entries[found]!!
            } else {
                noOp
            }
        }
    }
}

/**
 * Obtains the string to be prepended before logging statements for the classes
 * [belonging][get] to this `LoggingDomain`.
 *
 * If the logging domain is not defined for a class, logging statements for it
 * will not be prefixed. Otherwise, the prefix would be the name of the logging domain
 * in square brackets followed by a space.
 */
public val LoggingDomain.messagePrefix: String
    get() = if (name.isEmpty()) "" else "[$name] "

/**
 * Obtains a name of a package of this Kotlin class.
 */
private val KClass<*>.packageName: String?
    get() = qualifiedName?.substringBeforeLast('.')
