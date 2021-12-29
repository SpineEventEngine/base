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

package io.spine.io

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * A [GLOB](https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob) pattern for
 * matching file paths.
 *
 * @see java.nio.file.FileSystem.getPathMatcher
 */
public class Glob(pattern: String) {

    init {
        require(pattern.isNotEmpty())
    }

    private val matcher: PathMatcher = FileSystems.getDefault()
        .getPathMatcher("glob:$pattern")

    public companion object {

        /**
         * A pattern which matched any file.
         */
        public val any: Glob = Glob("**")

        /**
         * A pattern which matches any file with the given extension.
         *
         * @param extension
         *         a file extension with or without the leading dot
         */
        @JvmStatic
        public fun extension(extension: CharSequence): Glob {
            val ext = if (extension.isEmpty()) extension
            else if (extension[0] == '.') extension.substring(1) else extension
            return Glob("**.$ext")
        }
    }

    /**
     * Checks if the given path matches this pattern.
     */
    public fun matches(path: Path): Boolean = matcher.matches(path)
}
