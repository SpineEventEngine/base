/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.code.proto.DescriptorReference.loadFromResources;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.knownTypesRef;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.randomRef;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.smokeTestModelCompilerRef;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Descriptor reference should")
@ExtendWith(TempDirectory.class)
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
        DescriptorReference firstReference = randomRef();
        firstReference.writeTo(path);
        assertResourcesLoaded(path, firstReference);

        DescriptorReference secondReference = knownTypesRef();
        secondReference.writeTo(path);
        assertResourcesLoaded(path, firstReference, secondReference);

        DescriptorReference thirdReference = smokeTestModelCompilerRef();
        thirdReference.writeTo(path);
        assertResourcesLoaded(path, firstReference, secondReference, thirdReference);
    }

    @Test
    @DisplayName("write a reference with expected content")
    void properContent(@TempDir Path path) throws IOException {
        DescriptorReference knownTypes = knownTypesRef();
        knownTypes.writeTo(path);

        File descRef = path.resolve(DescriptorReference.FILE_NAME)
                           .toFile();
        List<String> linesWritten = Files.readLines(descRef, UTF_8);
        assertEquals(1, linesWritten.size());
        String actual = linesWritten.get(0);
        String expected = knownTypes.asResource()
                                    .toString();
        assertEquals(expected, actual);
    }

    private static void assertDescriptorRefsWrittenCorrectly(@TempDir Path path,
                                                             String separator,
                                                             DescriptorReference... descriptors) {
        for (DescriptorReference descriptor : descriptors) {
            descriptor.writeTo(path, separator);
        }

        assertResourcesLoaded(path, descriptors);
    }

    @Test
    @DisplayName("throw if the referenced path points to a file instead of a directory")
    void throwsOnDirectory(@TempDir Path path) {
        DescriptorReference knownTypes = knownTypesRef();
        File newFile = createFileUnderPath(path);
        assertThrows(IllegalStateException.class, () -> knownTypes.writeTo(newFile.toPath()));
    }

    @Test
    @DisplayName("throw if the referenced path is null")
    void throwsOnNull() {
        DescriptorReference knownTypes = knownTypesRef();
        assertThrows(NullPointerException.class, () -> knownTypes.writeTo(null));
    }

    @Test
    @DisplayName("return an empty iterator upon missing `desc.ref` file")
    void onMissingDescRef() {
        Iterator<Resource> result = DescriptorReference.loadFromResources(emptyList());
        assertFalse(result.hasNext());
    }

    private static void assertResourcesLoaded(Path path, DescriptorReference... expected) {
        Path descRef = path.resolve(DescriptorReference.FILE_NAME);
        Iterator<Resource> existingDescriptors = loadFromResources(asList(descRef));
        List<Resource> result = newArrayList(existingDescriptors);
        assertEquals(expected.length, result.size());
        for (DescriptorReference reference : expected) {
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
        String fileName = UUID.randomUUID()
                              .toString();
        File result = new File(path.toFile(), fileName);
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
