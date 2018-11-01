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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.InvalidProtocolBufferException;
import io.spine.logging.Logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.ByteStreams.readFully;
import static com.google.common.io.Files.createParentDirs;
import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.option.Options.registry;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A descriptor set merger.
 */
public final class Merger implements Logging {

    private final ArchiveUnpacker fullUnpacker;

    public Merger(ArchiveUnpacker unpacker) {
        this.fullUnpacker = checkNotNull(unpacker);
    }

    /**
     * Merges the contents of the given files into a single descriptor set.
     *
     * <p>This method assumes that all the given files exist and contain instances of
     * {@link FileDescriptorSet} Protobuf message.
     *
     * @param files
     *         the files to merge
     * @return the {@link MergedDescriptorSet}
     */
    public MergedDescriptorSet merge(Collection<File> files) {
        FileDescriptorSet merged = readAllDescriptors(files)
                .stream()
                .reduce(FileDescriptorSet.newBuilder(),
                        FileDescriptorSet.Builder::mergeFrom,
                        (right, left) -> right.addAllFile(left.getFileList()))
                .build();
        MergedDescriptorSet result = new MergedDescriptorSet(merged);
        return result;
    }

    private static FileDescriptorSet parseDescriptorSet(byte[] fileDescriptorSet) {
        try {
            return FileDescriptorSet.parseFrom(fileDescriptorSet, registry());
        } catch (InvalidProtocolBufferException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private Set<FileDescriptorSet> readAllDescriptors(Collection<File> dependencies) {
        ImmutableSet.Builder<FileDescriptorSet> result = ImmutableSet.builder();
        for (File file : dependencies) {
            log().debug("Merging descriptors from `{}`.", file);
            if (file.isDirectory()) {
                mergeDirectory(file)
                        .ifPresent(result::add);
            } else {
                if (ZipArchiveExtension.anyMatch(file)) {
                    readFromArchive(file)
                            .ifPresent(result::add);
                } else {
                    readFromPlainFile(file)
                            .ifPresent(result::add);
                }
            }
        }
        return result.build();
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
                .map(Merger::read);
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

    /**
     * Unpacks {@code ZIP} archive files.
     */
    public interface ArchiveUnpacker {

        /**
         * Unpacks the given {@code archive} onto the disk.
         *
         * @param archive
         *         a {@code ZIP} archive file
         * @return a collection of the unpacked files
         */
        Collection<File> unpack(File archive);
    }

    /**
     * A view on a {@link FileDescriptorSet} after merging.
     */
    public static final class MergedDescriptorSet {

        private final FileDescriptorSet descriptorSet;

        private MergedDescriptorSet(FileDescriptorSet descriptorSet) {
            this.descriptorSet = descriptorSet;
        }

        /**
         * Writes this descriptor set into the given file.
         *
         * <p>If the file exists, it will be overridden. Otherwise, the file (and all its parent
         * directories if necessary) will be created.
         *
         * @param destination
         *         the file to write this descriptor set into
         */
        public void writeTo(File destination) {
            checkNotNull(destination);
            prepareFile(destination);
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))) {
                descriptorSet.writeTo(out);
            } catch (IOException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        private static void prepareFile(File destination) {
            try {
                destination.delete();
                createParentDirs(destination);
                destination.createNewFile();
            } catch (IOException e) {
                throw illegalArgumentWithCauseOf(e);
            }
        }
    }
}
