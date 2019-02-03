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

package io.spine.tools.compiler.descriptor;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.code.proto.FileDescriptorSets;
import io.spine.logging.Logging;
import io.spine.tools.archive.ArchiveEntry;
import io.spine.tools.archive.ArchiveFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.tools.archive.ArchiveFile.isArchive;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toSet;

/**
 * A set of {@code FileDescriptorSet}s.
 */
public final class FileDescriptorSuperset implements Logging {

    private final Set<FileDescriptorSet> descriptors;

    /**
     * Creates a new instance of {@code FileDescriptorSuperset}.
     */
    public FileDescriptorSuperset() {
        this.descriptors = newHashSet();
    }

    /**
     * Flattens this superset into a single descriptor set.
     *
     * <p>The descriptors in the output set are de-duplicated and unordered.
     *
     * @return the result of the sets merging
     */
    public MergedDescriptorSet merge() {
        Set<FileDescriptorProto> allFiles = descriptors
                .stream()
                .flatMap(set -> set.getFileList().stream())
                .collect(toSet());
        FileDescriptorSet descriptorSet = FileDescriptorSet
                .newBuilder()
                .addAllFile(allFiles)
                .build();
        return new MergedDescriptorSet(descriptorSet);
    }

    public void addFromDependency(File dependencyFile) {
        readDependency(dependencyFile)
                .ifPresent(this::addFiles);
    }

    private void addFiles(FileDescriptorSet fileSet) {
        descriptors.add(fileSet);
    }

    private Optional<FileDescriptorSet> readDependency(File file) {
        log().debug("Merging descriptors from `{}`.", file);
        if (file.isDirectory()) {
            return mergeDirectory(file);
        } else if (isArchive(file)) {
            return readFromArchive(file);
        } else {
            return readFromPlainFile(file);
        }
    }

    private static Optional<FileDescriptorSet> mergeDirectory(File directory) {
        File[] knownTypesFile = directory.listFiles(
                (dir, name) -> KNOWN_TYPES.equals(name)
        );
        checkNotNull(knownTypesFile);
        if (knownTypesFile.length == 0) {
            return Optional.empty();
        } else if (knownTypesFile.length == 1) {
            return readFromPlainFile(knownTypesFile[0]);
        } else {
            throw newIllegalStateException("Multiple descriptor files found in %s.",
                                           directory.getPath());
        }
    }

    private static Optional<FileDescriptorSet> readFromArchive(File archiveFile) {
        ArchiveFile archive = ArchiveFile.from(archiveFile);
        return archive.findEntry(KNOWN_TYPES)
                      .map(ArchiveEntry::asDescriptorSet);
    }

    private static FileDescriptorSet read(File file) {
        checkArgument(file.exists());
        Path path = file.toPath();
        try {
            byte[] bytes = Files.readAllBytes(path);
            return FileDescriptorSets.parse(bytes);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static Optional<FileDescriptorSet> readFromPlainFile(File file) {
        if (KNOWN_TYPES.equals(file.getName())) {
            FileDescriptorSet result = read(file);
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
