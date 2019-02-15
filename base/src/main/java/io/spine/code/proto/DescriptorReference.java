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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.spine.io.Files2;
import io.spine.io.ResourceFiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Streams.stream;
import static com.google.common.io.ByteStreams.toByteArray;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Reference to descriptor set files.
 *
 * <p>Multiple reference files may be present at runtime of an application. The files may be merged
 * by appending if a "fat" JAR artifact is required.
 */
public final class DescriptorReference {

    @VisibleForTesting
    static final String FILE_NAME = "desc.ref";

    @SuppressWarnings("HardcodedLineSeparator")     /* Use pre-defined separator to eliminate
                                                       platform-dependent issues in `desc.ref`.*/
    private static final String SEPARATOR = "\n";
    private static final Splitter LINE_SPLITTER = Splitter.on(SEPARATOR)
                                                          .omitEmptyStrings()
                                                          .trimResults();
    private final String reference;

    private DescriptorReference(String reference) {
        this.reference = reference;
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
     * Writes this reference into the {@code desc.ref} file under the given directory.
     *
     * <p>Appends the file if it already exists or creates a new file otherwise.
     *
     * @param directory
     *         target dir for the {@code desc.ref} file
     */
    public void writeTo(Path directory) {
        checkNotNull(directory);
        Path targetFile = directory.resolve(FILE_NAME);
        Files2.ensureFile(targetFile);
        try {
            List<String> resources = Files.readAllLines(targetFile);
            resources.add(reference);
            String result = String.join(SEPARATOR, resources)
                                  .trim();
            Files.write(targetFile, ImmutableList.of(result), TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * A testing method that reproduces an illegal situation - a reference gets appended, but with
     * some newline symbol at the end. This should not affect extraction of resources.
     */
    @VisibleForTesting
    void writeToWithSeparator(Path directory, String separator) {
        checkNotNull(directory);
        Path targetFile = directory.resolve(FILE_NAME);
        Files2.ensureFile(targetFile);
        try {
            List<String> resources = Files.readAllLines(targetFile);
            resources.add(reference + separator);
            String result = String.join(SEPARATOR, resources)
                                  .trim();
            Files.write(targetFile, ImmutableList.of(result), TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Loads all the referenced descriptor set files from the current classpath.
     *
     * @return an iterator over application resources
     */
    static Iterator<ResourceReference> loadAll() {
        return loadFromResources(ResourceFiles.loadAll(FILE_NAME));
    }

    @VisibleForTesting
    static Iterator<ResourceReference> loadFromResources(Iterator<URL> resources) {
        return stream(resources)
                .map(DescriptorReference::readCatalog)
                .flatMap(catalog -> LINE_SPLITTER.splitToList(catalog)
                                                 .stream())
                .distinct()
                .map(ResourceReference::new)
                .iterator();
    }

    private static String readCatalog(URL resourceUrl) {
        try (InputStream catalogStream = resourceUrl.openStream()) {
            byte[] catalogBytes = toByteArray(catalogStream);
            String catalog = new String(catalogBytes, UTF_8);
            return catalog;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    ResourceReference asResource() {
        return new ResourceReference(reference);
    }

    /**
     * A reference to an application resource.
     */
    public static final class ResourceReference {

        private final String resourceName;

        private ResourceReference(String resourceName) {
            this.resourceName = resourceName;
        }

        /**
         * Opens an {@code InputStream} for this resource.
         *
         * @return the resource stream or {@code Optional.empty()} if the resource does not exist
         */
        public Optional<InputStream> openStream() {
            InputStream result = DescriptorReference.class.getClassLoader()
                                                          .getResourceAsStream(resourceName);
            return Optional.ofNullable(result);
        }

        @Override
        public String toString() {
            return resourceName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ResourceReference reference = (ResourceReference) o;
            return Objects.equals(resourceName, reference.resourceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceName);
        }
    }
}
