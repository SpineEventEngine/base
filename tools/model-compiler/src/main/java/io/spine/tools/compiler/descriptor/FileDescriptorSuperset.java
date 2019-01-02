/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.InvalidProtocolBufferException;
import io.spine.logging.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.io.ByteStreams.readFully;
import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.option.Options.registry;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.stream.Collectors.toSet;

/**
 * A set of {@code FileDescriptorSet}s.
 */
public final class FileDescriptorSuperset implements Logging {

    private final ArchiveUnpacker fullUnpacker;

    private final Collection<FileDescriptorSet> descriptors;

    /**
     * Creates a new instance of {@code FileDescriptorSuperset}.
     *
     * @param unpacker
     *         an {@link ArchiveUnpacker} to tackle archive files which cannot be comprehended
     *         via their ZIP tree
     */
    public FileDescriptorSuperset(ArchiveUnpacker unpacker) {
        this.fullUnpacker = checkNotNull(unpacker);
        this.descriptors = newLinkedList();
    }

    public MergedDescriptorSet merge() {
        Set<DescriptorProtos.FileDescriptorProto> allFiles = descriptors
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
        } else if (ZipArchiveExtension.anyMatch(file)) {
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

    private Optional<FileDescriptorSet> readFromArchive(File archive) {
        try (
                FileInputStream fileStream = new FileInputStream(archive);
                ZipInputStream stream = new ZipInputStream(fileStream)
        ) {
            return readFromArchive(stream, archive);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Optional<FileDescriptorSet> readFromArchive(ZipInputStream stream, File archive)
            throws IOException {
        for (ZipEntry entry = stream.getNextEntry();
             entry != null;
             entry = stream.getNextEntry()) {
            if (KNOWN_TYPES.equals(entry.getName())) {
                log().debug("Merging ZIP entry `{}` of size: {}.", entry, entry.getSize());
                return readFromEntry(entry, stream, archive);
            }
        }
        return Optional.empty();
    }

    private Optional<FileDescriptorSet> readFromEntry(ZipEntry entry, ZipInputStream stream,
                                                      File archive)
            throws IOException {
        @SuppressWarnings("NumericCastThatLosesPrecision") // The expected file should fit.
        int size = (int) entry.getSize();
        if (size < 0) { // Cannot read the entry size correctly.
            return readFromArchiveByUnpacking(archive);
        } else {
            return Optional.of(readFromCurrentEntry(stream, size));
        }
    }

    private static FileDescriptorSet readFromCurrentEntry(ZipInputStream stream, int entrySize)
            throws IOException {
        byte[] buffer = new byte[entrySize];
        readFully(stream, buffer);
        FileDescriptorSet parsed = parseDescriptorSet(buffer);
        return parsed;
    }

    private Optional<FileDescriptorSet> readFromArchiveByUnpacking(File archive) {
        Optional<FileDescriptorSet> result = fullUnpacker
                .unpack(archive)
                .stream()
                .filter(file -> KNOWN_TYPES.equals(file.getName()))
                .findAny()
                .map(FileDescriptorSuperset::read);
        return result;
    }

    private static FileDescriptorSet read(File file) {
        checkArgument(file.exists());
        Path path = file.toPath();
        try {
            byte[] bytes = Files.readAllBytes(path);
            return parseDescriptorSet(bytes);
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

    private static FileDescriptorSet parseDescriptorSet(byte[] fileDescriptorSet) {
        try {
            return FileDescriptorSet.parseFrom(fileDescriptorSet, registry());
        } catch (InvalidProtocolBufferException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
