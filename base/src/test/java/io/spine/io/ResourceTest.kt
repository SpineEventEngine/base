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

import com.google.common.io.CharStreams
import com.google.common.testing.NullPointerTester
import com.google.common.truth.Truth.assertThat
import io.spine.io.Copy.copyContent
import io.spine.testing.Assertions.assertIllegalState
import io.spine.testing.TestValues
import java.io.InputStream
import java.nio.file.Files.exists
import java.nio.file.Files.isDirectory
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.listDirectoryEntries
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`Resource` should")
class ResourceTest {

    private lateinit var resource: Resource

    companion object {
        private const val resourceFile = "test_resource.txt"
        private val classLoader = ResourceTest::class.java.classLoader
    }

    @BeforeEach
    fun createResource() {
        resource = Resource.file(resourceFile, classLoader)
    }

    @Test
    fun `handle 'null' args`() {
        NullPointerTester()
            .setDefault(ClassLoader::class.java, classLoader)
            .testAllPublicStaticMethods(Resource::class.java)
    }

    @Test
    fun `throw ISE if queried for a non-existing file`() {
        val name = TestValues.randomString()
        val file = Resource.file(name, classLoader)
        assertThat(file.exists()).isFalse()
        assertIllegalState { file.locate() }
    }

    @Test
    fun `identify a file under the resources directory`() {
        assertThat(resource)
            .isNotNull()
        assertThat(resource.exists())
            .isTrue()
        assertThat(resource.locate())
            .isNotNull()
        assertThat(resource.locateAll())
            .hasSize(1)
        resource.open().use { stream -> assertNotEmpty(stream) }
    }

    @Test
    fun `open as a byte stream`() {
        resource.open().use { stream -> assertNotEmpty(stream) }
    }

    private fun assertNotEmpty(stream: InputStream) {
        assertThat(stream.available()).isGreaterThan(0)
    }

    @Test
    fun `open as a char stream`() {
        resource.openAsText().use { reader ->
            val content = CharStreams.toString(reader)
            assertThat(content).isNotEmpty()
        }
    }

    @Test
    fun `expose a directory for copying`(@TempDir tempDir: Path) {
        val directory = Resource.file("directory", classLoader)
        val path = Paths.get(directory.locate()!!.path)

        assertTrue(isDirectory(path))
        
        copyContent(path, tempDir)

        assertThat(tempDir.listDirectoryEntries())
            .isNotEmpty()

        fun assertExists(relativePath: String) {
            val fullPath = tempDir.resolve(relativePath)
            assertTrue(exists(fullPath), "Expected to exist: `${fullPath}`.")
        }

        listOf(
            ".dot-file",
            "file1.txt",

            "subdir/.dot-file",
            "subdir/file1.txt",
            "subdir/file2.txt",

            "subdir/sub-sub-dir/.dot-file",
            "subdir/sub-sub-dir/file1.txt",
            "subdir/sub-sub-dir/file2.txt",
            "subdir/sub-sub-dir/file3.txt",
        ).forEach { p -> assertExists(p) }
    }
}
