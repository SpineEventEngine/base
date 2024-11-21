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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.io.Resource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.ByteStreams.toByteArray;
import static io.spine.io.Ensure.ensureFile;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Reference to descriptor set files.
 *
 * <p>Multiple reference files may be present at the classpath of an application.
 * The files may be merged by appending if a "fat" JAR artifact is required.
 */
@Immutable
public final class DescriptorReference {

    /**
     * A file that contains references to a number of Protobuf descriptor sets.
     *
     * <p>There may be multiple `desc.ref` files present in one project.
     */
    private static final String FILE_NAME = "desc.ref";

    /**
     * The resource file with the {@link #FILE_NAME}.
     */
    private static final Resource FILE_IN_CLASSPATH =
            Resource.file(FILE_NAME, DescriptorReference.class.getClassLoader());

    @SuppressWarnings(
            "HardcodedLineSeparator"
            /* Use pre-defined separator to eliminate platform-dependent issues in `desc.ref`.*/
    )
    private static final String SEPARATOR = "\n";
    private static final Splitter LINE_SPLITTER = Splitter.on(SEPARATOR)
                                                          .omitEmptyStrings()
                                                          .trimResults();
    /**
     * The name of the descriptor set file to which this reference points.
     */
    private final String reference;

    private DescriptorReference(String reference) {
        this.reference = reference;
    }

    /**
     * Obtains a reference to the descriptor reference file in the given directory.
     *
     * <p>The file may not exist yet.
     */
    public static File fileAt(Path directory) {
        return directory.resolve(FILE_NAME).toFile();
    }

    /**
     * Creates a new reference to the given file.
     *
     * @param file
     *         the descriptor set file to reference
     * @return new instance
     */
    public static DescriptorReference toOneFile(File file) {
        checkNotNull(file);
        return new DescriptorReference(file.getName());
    }

    /**
     * Loads all the referenced descriptor set files from the current classpath.
     *
     * @return an iterator over application resources
     */
    static Iterator<Resource> loadAll() {
        return loadFromResources(FILE_IN_CLASSPATH.locateAll());
    }

    /**
     * Loads all the referenced descriptor set files from the specified iterator of {@code URL}s.
     *
     * @param resources
     *         {@code URL}s that contain referenced to descriptor sets
     * @return an {@code Iterator} of referenced to resources described by the given {@code
     *         resources} iterator
     */
    @VisibleForTesting
    static Iterator<Resource> loadFromResources(Collection<URL> resources) {
        var classLoader = DescriptorReference.class.getClassLoader();
        return resources.stream()
                .map(DescriptorReference::readCatalog)
                .flatMap(catalog -> LINE_SPLITTER.splitToList(catalog).stream())
                .distinct()
                .map(name -> Resource.file(name, classLoader))
                .iterator();
    }

    private static String readCatalog(URL resource) {
        try (var catalogStream = resource.openStream()) {
            var catalogBytes = toByteArray(catalogStream);
            var catalog = new String(catalogBytes, UTF_8);
            return catalog;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Writes this reference into the {@code desc.ref} file under the given directory.
     *
     * <p>Appends this reference to the file if it already exists or creates a new file otherwise.
     *
     * @param directory
     *         target dir for the {@code desc.ref} file
     */
    public void writeTo(Path directory) {
        checkNotNull(directory);
        writeTo(directory, null);
    }

    /**
     * Writes this reference to a {@code desc.ref} file under the specified directory,
     * appending the optional trailing text.
     *
     * @param trail
     *         the test-only parameter used for testing of reference files that can get
     *         a forced system-dependent line separator added by development tools such as Git.
     */
    @VisibleForTesting
    void writeTo(Path directory, @VisibleForTesting @Nullable String trail) {
        checkNotNull(directory);
        var targetFile = fileAt(directory).toPath();
        ensureFile(targetFile);
        try {
            var resources = Files.readAllLines(targetFile);
            resources.add(reference);
            if (trail != null) {
                resources.add(trail);
            }
            var result = String.join(SEPARATOR, resources)
                               .trim();
            Files.write(targetFile, ImmutableList.of(result), TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Obtains a {@code ResourceReference} that is described by this descriptor reference.
     */
    @VisibleForTesting
    Resource asResource() {
        return Resource.file(reference, getClass().getClassLoader());
    }
}
