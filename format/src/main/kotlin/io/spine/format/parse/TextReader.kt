/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.format.parse

import com.google.common.io.ByteSource
import java.nio.charset.Charset

/**
 * A reader for the plain text file into [java.lang.String].
 *
 * This object does not parse, but simply reads a [ByteSource] instance into a string.
 *
 * The string is converted from bytes using the [default charset][Charset.defaultCharset].
 */
internal data object TextReader : Parser {

    /**
     * Reads the given [source] as [java.lang.String] value.
     *
     * To ensure the type safety, the [cls] parameter is checked to
     * be the class of [java.lang.String].
     *
     * @throws IllegalStateException if the type [T] is not [java.lang.String].
     */
    override fun <T> parse(source: ByteSource, cls: Class<T>): T {
        if (cls != String::class.java) {
            error("Expected format supporting the type `${cls.canonicalName}`" +
                    " but got a text file.")
        }
        val value = source.asCharSource(Charset.defaultCharset()).read()
        @Suppress("UNCHECKED_CAST")
        return value as T
    }
}
