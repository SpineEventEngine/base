/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.proto;

import com.google.common.truth.IterableSubject;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.type.PersonProto;
import io.spine.tools.type.ProjectProto;
import io.spine.tools.type.TaskProto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.io.Files.createParentDirs;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.Files.createFile;
import static java.util.stream.Collectors.toList;

@DisplayName("FileDescriptorSuperset should")
class FileDescriptorSupersetTest {

    /**
     * A directory with a {@code known_types.desc} file in it.
     */
    private Path directoryDependency;

    /**
     * A {@code known_types.desc} file.
     */
    private Path fileDependency;

    /**
     * A ZIP archive with a {@code known_types.desc} file in it.
     */
    private Path archiveDependency;

    /**
     * An empty {@code known_types.desc} file.
     */
    private Path emptyFileDependency;

    /**
     * A ZIP archive with NO {@code known_types.desc} file in it.
     */
    private Path archiveWithNoDescriptors;

    /**
     * A file which is NOT a {@code known_types.desc} file.
     */
    private Path nonDescriptorFile;

    /**
     * Copies the test data from the resources of this test suite to the specified temp directory.
     *
     * <p>The test cases may access the created files via the fields of this class. Each field of
     * type {@code Path} represents an existing file system object which may or may not contain
     * a descriptor set file.
     *
     * @param sandbox a temp directory created by the {@code TempDirectory} extension
     */
    @BeforeEach
    void setUp(@TempDir Path sandbox) throws IOException {
        Path directoryDependencyFile = sandbox.resolve("dir")
                                              .resolve(KNOWN_TYPES);
        fileDependency = sandbox.resolve(KNOWN_TYPES);
        archiveDependency = sandbox.resolve("zipped_descriptors.zip");
        emptyFileDependency = sandbox.resolve("empty-descriptor")
                                     .resolve(KNOWN_TYPES);
        directoryDependency = directoryDependencyFile.getParent();
        archiveWithNoDescriptors = sandbox.resolve("irrelevant.zip");
        writeDescriptorSet(fileDependency, PersonProto.getDescriptor());
        writeDescriptorSet(directoryDependencyFile, ProjectProto.getDescriptor());
        writeDescriptorSet(directoryDependencyFile, ProjectProto.getDescriptor());
        writeDescriptorSetToZip(archiveDependency, TaskProto.getDescriptor());
        writeDescriptorSet(emptyFileDependency);
        writeIrrelevantZip(archiveWithNoDescriptors);

        nonDescriptorFile = sandbox.resolve("non-desc");
        createFile(nonDescriptorFile);
    }

    @Test
    @DisplayName("merge descriptors from ZIP, folder, and standalone file")
    void mergeFromAllSources() {
        FileDescriptorSuperset superset = new FileDescriptorSuperset();
        superset.addFromDependency(directoryDependency.toFile());
        superset.addFromDependency(fileDependency.toFile());
        superset.addFromDependency(archiveDependency.toFile());

        MergedDescriptorSet mergedSet = superset.merge();
        IterableSubject assertDescriptors = assertThat(mergedSet.descriptors());
        assertDescriptors.hasSize(3);
        assertDescriptors.contains(TaskProto.getDescriptor().toProto());
        assertDescriptors.contains(PersonProto.getDescriptor().toProto());
        assertDescriptors.contains(ProjectProto.getDescriptor().toProto());
    }

    @Test
    @DisplayName("ignore empty files")
    void ignoreEmptyFiles() {
        FileDescriptorSuperset superset = new FileDescriptorSuperset();
        superset.addFromDependency(emptyFileDependency.toFile());
        assertThat(superset.merge().descriptors()).isEmpty();
    }

    @Test
    @DisplayName("ignore ZIPs with no descriptors files")
    void ignoreIrrelevantZips() {
        FileDescriptorSuperset superset = new FileDescriptorSuperset();
        superset.addFromDependency(archiveWithNoDescriptors.toFile());
        assertThat(superset.merge().descriptors()).isEmpty();
    }

    @Test
    @DisplayName("ignore non-descriptor files")
    void ignoreNonDescriptorFiles() {
        FileDescriptorSuperset superset = new FileDescriptorSuperset();
        superset.addFromDependency(nonDescriptorFile.toFile());
        assertThat(superset.merge().descriptors()).isEmpty();
    }

    private static void writeDescriptorSet(Path path, FileDescriptor... fileDescriptor)
            throws IOException {
        File destination = path.toFile();
        createParentDirs(destination);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destination))) {
            FileDescriptorSet descriptorSet = descriptorSet(fileDescriptor);
            out.write(descriptorSet.toByteArray());
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static void writeDescriptorSetToZip(Path zipPath, FileDescriptor... fileDescriptor) {
        File destination = zipPath.toFile();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination))) {
            FileDescriptorSet descriptorSet = descriptorSet(fileDescriptor);
            ZipEntry descEntry = new ZipEntry(KNOWN_TYPES);
            out.putNextEntry(descEntry);
            out.write(descriptorSet.toByteArray());
            out.closeEntry();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static void writeIrrelevantZip(Path zipPath) {
        File destination = zipPath.toFile();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination))) {
            out.putNextEntry(new ZipEntry("foo.txt"));
            out.putNextEntry(new ZipEntry("bar.txt"));
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static FileDescriptorSet descriptorSet(FileDescriptor[] fileDescriptor) {
        List<FileDescriptorProto> descriptors = Arrays
                .stream(fileDescriptor)
                .map(FileDescriptor::toProto)
                .collect(toList());
        return FileDescriptorSet
                .newBuilder()
                .addAllFile(descriptors)
                .build();
    }
}
