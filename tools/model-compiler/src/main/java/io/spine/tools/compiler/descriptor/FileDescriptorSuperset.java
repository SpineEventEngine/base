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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.spine.code.proto.FileDescriptorSets;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.logging.Logging;
import io.spine.tools.compiler.archive.ArchiveEntry;
import io.spine.tools.compiler.archive.ArchiveFile;
import io.spine.tools.type.MoreKnownTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.newHashSet;
import static io.spine.code.proto.FileDescriptors.DESC_EXTENSION;
import static io.spine.tools.compiler.archive.ArchiveFile.isArchive;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.System.lineSeparator;
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

    public void loadIntoKnownTypes() {
        MergedDescriptorSet merged = merge();
        FileSet fileSet = FileSet.ofFiles(merged.descriptors());
        TypeSet typeSet = TypeSet.messagesAndEnums(fileSet);
        _debug("Adding new types to the KnownTypes:{}{}", lineSeparator(), typeSet.types());
        MoreKnownTypes.extendWith(typeSet);
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
        checkNotNull(dependencyFile);
        _debug("Loading descriptors from `{}`.", dependencyFile);
        readDependency(dependencyFile)
                .forEach(this::addFiles);
    }

    private void addFiles(FileDescriptorSet fileSet) {
        descriptors.add(fileSet);
    }

    private Collection<FileDescriptorSet> readDependency(File file) {
        if (file.isDirectory()) {
            return mergeDirectory(file);
        } else if (isArchive(file)) {
            return readFromArchive(file);
        } else {
            return readFromPlainFile(file)
                    .map(ImmutableSet::of)
                    .orElse(ImmutableSet.of());
        }
    }

    private Collection<FileDescriptorSet> mergeDirectory(File directory) {
        File[] descriptorFiles = directory.listFiles(
                (dir, name) -> name.endsWith(DESC_EXTENSION)
        );
        checkNotNull(descriptorFiles);
        if (descriptorFiles.length == 0) {
            return ImmutableSet.of();
        } else {
            ImmutableSet<FileDescriptorSet> descriptors = Stream.of(descriptorFiles)
                                                                .map(this::read)
                                                                .collect(toImmutableSet());
            return descriptors;
        }
    }

    private Collection<FileDescriptorSet> readFromArchive(File archiveFile) {
        ArchiveFile archive = ArchiveFile.from(archiveFile);
        ImmutableSet<FileDescriptorSet> result = archive.findByExtension(DESC_EXTENSION)
                                                         .stream()
                                                         .map(ArchiveEntry::asDescriptorSet)
                                                         .collect(toImmutableSet());
        if (!result.isEmpty()) {
            _debug("Found {} descriptor set file(s) in archive `{}`.", result.size(), archiveFile);
        }
        return result;
    }

    private FileDescriptorSet read(File file) {
        checkArgument(file.exists(), "File does not exist: `%s`.", file);
        Path path = file.toPath();
        _debug("Reading descriptors from file `{}`.", file);
        try {
            byte[] bytes = Files.readAllBytes(path);
            return FileDescriptorSets.parse(bytes);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Optional<FileDescriptorSet> readFromPlainFile(File file) {
        if (file.getName().endsWith(DESC_EXTENSION)) {
            FileDescriptorSet result = read(file);
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
