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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import io.spine.io.Resource;
import io.spine.util.Exceptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.code.proto.DescriptorReference.loadFromResources;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.knownTypesRef;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.randomRef;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.smokeTestModelCompilerRef;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.Assertions.assertNpe;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`DescriptorReference` should")
class DescriptorReferenceTest {

    @SuppressWarnings("HardcodedLineSeparator")
    private static final String WINDOWS_SEPARATOR = "\r\n"; /* Resistance to different separators is
                                                             * a part of the test. */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String UNIX_SEPARATOR = "\n";

    @Test
    @DisplayName("be unaffected by Windows line separator")
    void unaffectedByCrLf(@TempDir Path path) {
        assertDescriptorRefsWrittenCorrectly(path, WINDOWS_SEPARATOR, knownTypesRef(), randomRef());
    }

    @Test
    @DisplayName("be unaffected by Unix line separator")
    void unaffectedByLf(@TempDir Path path) {
        assertDescriptorRefsWrittenCorrectly(path, UNIX_SEPARATOR, knownTypesRef(), randomRef());
    }

    @Test
    @DisplayName("ignore previous content of the `desc.ref` file")
    void ignorePreviousDescRef(@TempDir Path path) {
        var firstReference = randomRef();
        firstReference.writeTo(path);
        assertResourcesLoaded(path, firstReference);

        var secondReference = knownTypesRef();
        secondReference.writeTo(path);
        assertResourcesLoaded(path, firstReference, secondReference);

        var thirdReference = smokeTestModelCompilerRef();
        thirdReference.writeTo(path);
        assertResourcesLoaded(path, firstReference, secondReference, thirdReference);
    }

    @Test
    @DisplayName("write a reference with expected content")
    void properContent(@TempDir Path path) throws IOException {
        var knownTypes = knownTypesRef();
        knownTypes.writeTo(path);

        var descRef = path.resolve(DescriptorReference.FILE_NAME).toFile();
        var linesWritten = Files.readLines(descRef, UTF_8);
        assertEquals(1, linesWritten.size());
        var fileName = linesWritten.get(0);
        assertThat(knownTypes.asResource().toString())
                .contains(fileName);
    }

    private static void assertDescriptorRefsWrittenCorrectly(@TempDir Path path,
                                                             String separator,
                                                             DescriptorReference... descriptors) {
        for (var descriptor : descriptors) {
            descriptor.writeTo(path, separator);
        }

        assertResourcesLoaded(path, descriptors);
    }

    @Test
    @DisplayName("throw if the referenced path points to a file instead of a directory")
    void throwsOnDirectory(@TempDir Path path) {
        var knownTypes = knownTypesRef();
        var newFile = createFileUnderPath(path);
        assertIllegalState(() -> knownTypes.writeTo(newFile.toPath()));
    }

    @Test
    @DisplayName("throw if the referenced path is null")
    void throwsOnNull() {
        var knownTypes = knownTypesRef();
        assertNpe(() -> knownTypes.writeTo(null));
    }

    @Test
    @DisplayName("return an empty iterator upon missing `desc.ref` file")
    void onMissingDescRef() {
        var result = DescriptorReference.loadFromResources(emptyList());
        assertFalse(result.hasNext());
    }

    private static void assertResourcesLoaded(Path path, DescriptorReference... expected) {
        var descRef = path.resolve(DescriptorReference.FILE_NAME);
        var existingDescriptors = loadFromResources(asList(descRef));
        List<Resource> result = newArrayList(existingDescriptors);
        assertEquals(expected.length, result.size());
        for (var reference : expected) {
            assertTrue(result.contains(reference.asResource()));
        }
    }

    private static ImmutableList<URL> asList(Path descRef) {
        try {
            return ImmutableList.of(descRef.toUri()
                                           .toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("TailRecursion")
    // As long as the specified path does not contain files with names matching a random UUID value,
    // recursive calls should not happen.
    private static File createFileUnderPath(Path path) {
        // Ensures no existing file with such name.
        var fileName = UUID.randomUUID().toString();
        var result = new File(path.toFile(), fileName);
        if (result.exists()) {
            return createFileUnderPath(path);
        }
        try {
            result.createNewFile();
            return result;
        } catch (IOException e) {
            throw Exceptions.newIllegalStateException(e,
                                                      "Could not create a temporary file in %s.",
                                                      path.toAbsolutePath());
        }
    }
}
