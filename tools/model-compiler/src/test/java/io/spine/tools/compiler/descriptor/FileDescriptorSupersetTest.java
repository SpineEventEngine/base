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

import com.google.common.truth.IterableSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static com.google.common.io.Files.createParentDirs;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.code.proto.FileDescriptors.KNOWN_TYPES;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(TempDirectory.class)
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
        Path directoryDependencyFile = sandbox.resolve("dir").resolve(KNOWN_TYPES);
        fileDependency = sandbox.resolve(KNOWN_TYPES);
        archiveDependency = sandbox.resolve("zipped_descriptors.zip");
        emptyFileDependency = sandbox.resolve("empty-descriptor").resolve(KNOWN_TYPES);
        writeResource("descriptors/dir/known_types.desc", directoryDependencyFile);
        directoryDependency = directoryDependencyFile.getParent();
        archiveWithNoDescriptors = sandbox.resolve("irrelevant.zip");
        writeResource("descriptors/known_types.desc", fileDependency);
        writeResource("descriptors/zipped_descriptors.zip", archiveDependency);
        writeResource("descriptors/empty.desc", emptyFileDependency);
        writeResource("descriptors/irrelevant.zip", archiveWithNoDescriptors);

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
        MergedDescriptorSet mergedSet = superset.merge();
        assertThat(mergedSet.descriptors()).isEmpty();
    }

    @Test
    @DisplayName("ignore ZIPs with no descriptors files")
    void ignoreIrrelevantZips() {
        FileDescriptorSuperset superset = new FileDescriptorSuperset();
        superset.addFromDependency(archiveWithNoDescriptors.toFile());
        MergedDescriptorSet mergedSet = superset.merge();
        assertThat(mergedSet.descriptors()).isEmpty();
    }

    @Test
    @DisplayName("ignore non-descriptor files")
    void ignoreNonDescriptorFiles() {
        FileDescriptorSuperset superset = new FileDescriptorSuperset();
        superset.addFromDependency(nonDescriptorFile.toFile());
        MergedDescriptorSet mergedSet = superset.merge();
        assertThat(mergedSet.descriptors()).isEmpty();
    }

    private static void writeResource(String resourceName, Path destination) throws IOException {
        File destinationFile = destination.toFile();
        createParentDirs(destinationFile);
        InputStream resource = FileDescriptorSupersetTest.class.getClassLoader()
                                                               .getResourceAsStream(resourceName);
        assertNotNull(resource);
        copy(resource, destination, REPLACE_EXISTING);
    }
}
