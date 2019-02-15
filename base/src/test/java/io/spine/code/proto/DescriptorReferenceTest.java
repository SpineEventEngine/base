/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.common.io.Resources;
import io.spine.code.proto.DescriptorReference.ResourceReference;
import io.spine.util.Exceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.code.proto.DescriptorReference.loadFromResources;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.toKnownTypes;
import static io.spine.code.proto.given.DescriptorReferenceTestEnv.toSmokeTestModelCompiler;
import static io.spine.util.Exceptions.newIllegalStateException;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Descriptor reference should")
class DescriptorReferenceTest {

    private static final Path PATH = Files.createTempDir()
                                          .toPath();

    @AfterEach
    void tearDown() throws IOException {
        MoreFiles.deleteRecursively(PATH, RecursiveDeleteOption.ALLOW_INSECURE);
    }

    @Test
    @DisplayName("be unaffected by Windows line separator")
    void unaffectedByCrLf() {
        DescriptorReference knownTypes = toKnownTypes().withCrLf();
        DescriptorReference smokeTestModelCompiler = toSmokeTestModelCompiler().withCrLf();
        knownTypes.writeTo(PATH);
        smokeTestModelCompiler.writeTo(PATH);

        assertExactAmount();
    }

    @Test
    @DisplayName("be unaffected by Unix line separator")
    void unaffectedByLf() {
        DescriptorReference knownTypes = toKnownTypes().withLf();
        DescriptorReference smokeTestModelCompiler = toSmokeTestModelCompiler().withLf();
        knownTypes.writeTo(PATH);
        smokeTestModelCompiler.writeTo(PATH);

        assertExactAmount();
    }

    @Test
    @DisplayName("throw if the referenced path points to a file instead of a directory")
    void throwsOnDirectory() {
        DescriptorReference knownTypes = toKnownTypes().withoutNewLine();
        File newFile = createFileUnderPath(PATH);
        assertThrows(IllegalStateException.class, () -> knownTypes.writeTo(newFile.toPath()));
    }

    @Test
    @DisplayName("throw if the referenced path is null")
    void throwsOnNull() {
        DescriptorReference knownTypes = toKnownTypes().withoutNewLine();
        assertThrows(NullPointerException.class, ()->knownTypes.writeTo(null));
    }

    private static void assertExactAmount() {
        Iterator<ResourceReference> existingDescriptors = loadFromResources(iterator());
        List<ResourceReference> result = newArrayList(existingDescriptors);
        assertEquals(2, result.size());
    }

    private static Iterator<URL> iterator() {
        File descRef = new File(PATH.toFile(), DescriptorReference.FILE_NAME);
        ImmutableList.Builder<URL> builder = ImmutableList.builder();
        try {
            List<String> descriptorReferences = Files.readLines(descRef, Charsets.UTF_8);
            for (String reference : descriptorReferences) {
                URL url = Resources.getResource(reference);
                builder.add(url);
            }
        } catch (IOException e) {
            throw newIllegalStateException(e, "Cannot compose URL from %s",
                                           PATH.toAbsolutePath());
        }

        return builder.build()
                      .iterator();
    }

    @SuppressWarnings("TailRecursion")
    // As long as the specified path does not contain files with names matching a random UUID value,
    // recursive calls will not happen.
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
