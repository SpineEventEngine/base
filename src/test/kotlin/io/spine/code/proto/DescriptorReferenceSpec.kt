/*
 * Copyright 2024, TeamDev. All rights reserved.
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

package io.spine.code.proto

import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.google.common.io.Resources
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.spine.io.Resource
import io.spine.testing.Assertions.assertIllegalState
import io.spine.testing.Assertions.assertNpe
import io.spine.util.Exceptions
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`DescriptorReference` should")
internal class DescriptorReferenceSpec {

    @Test
    fun `handle trailing Windows line separator`(@TempDir path: Path) {
        assertDescriptorRefsWrittenCorrectly(
            path,
            WINDOWS_SEPARATOR,
            knownTypesRef(),
            randomRef()
        )
    }

    @Test
    fun `handle trailing Linux line separator`(@TempDir path: Path) {
        assertDescriptorRefsWrittenCorrectly(
            path,
            UNIX_SEPARATOR,
            knownTypesRef(),
            randomRef()
        )
    }

    @Test
    fun `ignore previous content of the file`(@TempDir path: Path) {
        val firstReference = randomRef()
        firstReference.writeTo(path)
        assertResourcesLoaded(path, firstReference)

        val secondReference = knownTypesRef()
        secondReference.writeTo(path)
        assertResourcesLoaded(path, firstReference, secondReference)

        val thirdReference = smokeTestModelCompilerRef()
        thirdReference.writeTo(path)
        assertResourcesLoaded(path, firstReference, secondReference, thirdReference)
    }

    @Test
    fun `write a reference with expected content`(@TempDir path: Path) {
        val knownTypes = knownTypesRef()
        knownTypes.writeTo(path)

        val descRef = DescriptorReference.fileAt(path)
        val linesWritten = Files.readLines(descRef, StandardCharsets.UTF_8)
        linesWritten.size shouldBe 1

        val fileName = linesWritten[0]
        knownTypes.asResource().toString() shouldContain fileName
    }

    @Test
    fun `reject reference to a file`(@TempDir path: Path) {
        val knownTypes = knownTypesRef()
        val newFile = createFileAt(path)
        assertIllegalState { knownTypes.writeTo(newFile.toPath()) }
    }

    @Test
    fun `reject 'null' destination directory`() {
        val knownTypes = knownTypesRef()
        assertNpe {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            knownTypes.writeTo(null)
        }
    }

    @Test
    fun `return an empty iterator upon missing file`() {
        val result = DescriptorReference.loadFromResources(emptyList())

        result.hasNext() shouldBe false
    }

    companion object {
        private const val WINDOWS_SEPARATOR = "\r\n"
        private const val UNIX_SEPARATOR = "\n"
    }
}

private fun assertDescriptorRefsWrittenCorrectly(
    @TempDir path: Path,
    separator: String,
    vararg descriptors: DescriptorReference
) {
    for (descriptor in descriptors) {
        descriptor.writeTo(path, separator)
    }
    assertResourcesLoaded(path, *descriptors)
}

private fun assertResourcesLoaded(path: Path, vararg expected: DescriptorReference) {
    val descRef = DescriptorReference.fileAt(path).toPath()
    val existingDescriptors = DescriptorReference.loadFromResources(asList(descRef))
    val result: List<Resource> = existingDescriptors.asSequence().toList()
    result.size shouldBe expected.size
    for (reference in expected) {
        result.contains(reference.asResource()) shouldBe true
    }
}

private fun asList(descRef: Path): ImmutableList<URL> {
    try {
        return ImmutableList.of(descRef.toUri().toURL())
    } catch (e: MalformedURLException) {
        throw IllegalStateException(e)
    }
}

/**
 * Creates a file with a random name at the given directory.
 *
 * In the unlikely event of already having the file with the random name,
 * repeats the attempt to create a file with another name.
 */
private fun createFileAt(dir: Path): File {
    val fileName = UUID.randomUUID().toString()
    val result = File(dir.toFile(), fileName)
    if (result.exists()) {
        return createFileAt(dir)
    }
    try {
        result.createNewFile()
        return result
    } catch (e: IOException) {
        throw Exceptions.newIllegalStateException(
            e,
            "Could not create a temporary file in %s.",
            dir.toAbsolutePath()
        )
    }
}

/** Returns a reference to a `"smoke-test-model-compiler.desc"` file.  */
private fun smokeTestModelCompilerRef(): DescriptorReference {
    val reference = "smoke_tests_model-compiler_tests_unspecified.desc"
    return DescriptorReference.toOneFile(File(reference))
}

/** Returns a reference to a `"known_types.desc"` file.  */
private fun knownTypesRef(): DescriptorReference {
    val asFile = Resources.getResource(FileDescriptors.KNOWN_TYPES).file
    val result = File(asFile)
    return DescriptorReference.toOneFile(result)
}

/**
 * Return a reference to a descriptor file with a random name.
 * Note that the returned file does not exist.
 */
private fun randomRef(): DescriptorReference {
    val reference = UUID.randomUUID().toString()
    val result = File(reference)
    return DescriptorReference.toOneFile(result)
}
