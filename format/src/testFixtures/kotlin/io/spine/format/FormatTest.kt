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

package io.spine.format

import io.spine.format.FormatTest.Companion.tempDir
import java.io.File
import java.util.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

/**
 * The abstract base for format tests that create a [file] under
 * automatically created [temporary directory][tempDir].
 *
 * @property format The format to test.
 * @property file The file path which is composed having the temporary directory
 *  as the parent, using a randomly generated UUID-based name.
 *  The file name has the extension supported by the given [format].
 *
 * @see computeFile
 */
abstract class FormatTest<T : Any>(
    protected val format: Format<T>
) {
    protected lateinit var file: File

    companion object {

        lateinit var tempDir: File

        @BeforeAll
        @JvmStatic
        fun createDirectory(@TempDir tempDir: File) {
            this.tempDir = tempDir
        }
    }

    @BeforeEach
    fun computeFile() {
        val name = UUID.randomUUID().toString()
        file = File(tempDir, name).ensureFormatExtension(format)
    }
}
