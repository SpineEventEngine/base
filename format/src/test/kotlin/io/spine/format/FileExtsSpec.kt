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

import io.kotest.matchers.shouldBe
import java.io.File
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`File` and `Path` extensions should")
internal class FileExtsSpec {

    @Test
    fun `identify files with supported formats`() {
        val supportedFiles = listOf(
            "file.binpb", "file.pb", "file.bin",
            "file.pb.json", "file.json", "file.yml", "file.yaml"
        )
        supportedFiles.forEach { fileName ->
            File(fileName).hasSupportedFormat() shouldBe true
        }
    }

    @Test
    fun `reject files with unsupported formats`() {
        val unsupportedFiles = listOf(
            "file.txt", "file.jpeg", "file.gif", "file.exe"
        )
        unsupportedFiles.forEach { fileName ->
            File(fileName).hasSupportedFormat() shouldBe false
        }
    }

    @Nested
    inner class `'ensureFileExtension' function` {

        @Test
        fun `adds or replaces extension`() {
            val name = "example"
            val names = arrayOf(
                name,           // No extension.
                "$name.txt",    // Unsupported.
                "$name.json",   // Supported.
                "$name.yaml",   // Also supported.
                "$name.pb.json" // Special case of "complex" supported extension.
            )
            val files = names.map { File(it) }

            Format.entries.forEach { format ->
                files.forEach { file ->
                    format.matches(file.ensureFormatExtension(format))
                }
            }
        }

        @Test
        fun `returning the same instance if the extension is already present`() {
            val file = File("my.yaml")
            (file.ensureFormatExtension(Format.Yaml) === file) shouldBe true
        }
    }
}
