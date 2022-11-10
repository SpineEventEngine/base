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

package io.spine.testing

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.lang.AssertionError
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

@DisplayName("`MoreAssertions` (`Assertions.kt`) should")
internal class AssertionsKtSpec {

    companion object {

        lateinit var dir: File
        lateinit var file: File

        val missingFile = File("nowhere-near")

        @JvmStatic
        @BeforeAll
        fun createDirectory(@TempDir tempDir: File) {
            dir = tempDir
            file = dir.resolve("test.txt")
            file.writeText("Foo bar")
        }
    }

    @Nested
    inner class `assert existence of a 'File'` {

        @Nested
        inner class `not throwing when exists` {

            @Test
            fun file() = assertDoesNotThrow {
                assertExists(file)
            }

            @Test
            fun directory() = assertDoesNotThrow {
                assertExists(dir)
            }

            @Test
            fun `file as 'Path'`() = assertDoesNotThrow {
                assertExists(file.toPath())
            }

            @Test
            fun `directory as 'Path'`() = assertDoesNotThrow {
                assertExists(dir.toPath())
            }
        }

        @Nested
        inner class `throwing when does not exist with` {

            @Test
            fun `default message`() {
                val exception = assertThrows<AssertionError> {
                    assertExists(missingFile)
                }
                assertThat(exception).hasMessageThat().run {
                    contains(missingFile.name)
                    contains("expected to exist")
                    contains("but it does not")
                }
            }

            @Test
            fun `custom message`() {
                val exception = assertThrows<AssertionError> {
                    assertExists(missingFile, "Could not locate `$missingFile`.")
                }
                assertThat(exception).hasMessageThat().run {
                    contains(missingFile.name)
                    contains("Could not locate")
                    contains("`.")
                }
            }
        }
    }
}
