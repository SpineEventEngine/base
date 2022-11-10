/*
 * Copyright 2022, TeamDev. All rights reserved.
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

@file:JvmName("MoreAssertions")

package io.spine.testing

import io.spine.testing.Assertions.hasPrivateParameterlessCtor
import java.io.File
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * This file extends assertions provided in Java class [Assertions]
 * with those for Kotlin, while providing Java compatibility where it's possible.
 */
@Suppress("unused")
private const val ABOUT = ""

/**
 * Asserts that the given [file or directory][fileOrDir] exists.
 *
 * @param fileOrDir
 *         the file or directory to check.
 * @param message
 *         an optional error message. If not specified, the default message with the name
 *         of the file will be shown.
 */
@JvmOverloads
public fun assertExists(fileOrDir: File, message: String? = null) {
    FileExist(fileOrDir, message).check()
}

/**
 * Asserts that the given [file or directory][fileOrDir] exists.
 *
 * @param fileOrDir
 *         the file or directory to check.
 * @param message
 *         an optional error message. If not specified, the default message with the name
 *         of the file will be shown.
 */
@JvmOverloads
public fun assertExists(fileOrDir: Path, message: String? = null) {
    FileExist(fileOrDir, message).check()
}

/**
 * Asserts that the given [file or directory][fileOrDir] does not exist.
 *
 * @param fileOrDir
 *         the file or directory to check.
 * @param message
 *         an optional error message. If not specified, the default message with the name
 *         of the file will be shown.
 */
@JvmOverloads
public fun assertDoesNotExist(fileOrDir: File, message: String? = null) {
    FileExist(fileOrDir, message, not = true).check()
}

/**
 * Asserts that the given [file or directory][fileOrDir] does not exist.
 *
 * @param fileOrDir
 *         the file or directory to check.
 * @param message
 *         an optional error message. If not specified, the default message with the name
 *         of the file will be shown.
 */
@JvmOverloads
public fun assertDoesNotExist(fileOrDir: Path, message: String? = null) {
    FileExist(fileOrDir, message, not = true).check()
}

/**
 * Asserts file existence.
 *
 * @param fileOrDir
 *         an instance of [File] or [Path] to check.
 * @param message
 *         an optional error message. If not specified, the default message with the name
 *         of the file or directory will be shown.
 * @param not
 *         if `true` the object checks that the file does NOT exist.
 */
private class FileExist(
    private val fileOrDir: Any,
    private val message: String? = null,
    private val not: Boolean = false) {

    private fun message(): String = message ?: if (not) {
        "`$fileOrDir` should not exist, but it does."
    } else {
        "`$fileOrDir` expected to exist, but it does not."
    }

    private fun file(): File = when(fileOrDir) {
        is File ->  fileOrDir
        is Path -> fileOrDir.toFile()
        else -> error("$fileOrDir is neither `File` nor `Path`.")
    }

    fun check() {
        if (not) {
            assertFalse(message()) { file().exists() }
        } else {
            assertTrue(message()) { file().exists() }
        }
    }
}

/**
 * Tells if the class [C] has private constructor with no parameters.
 */
public inline fun <reified C: Any> hasPrivateParameterlessCtor(): Boolean =
    hasPrivateParameterlessCtor(C::class.java)


