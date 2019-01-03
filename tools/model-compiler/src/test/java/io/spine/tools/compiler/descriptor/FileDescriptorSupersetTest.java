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
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(TempDirectory.class)
@DisplayName("FileDescriptorSuperset should")
class FileDescriptorSupersetTest {

    private Path directoryDependency;
    private Path fileDependency;
    private Path archiveDependency;

    @BeforeEach
    void setUp(@TempDir Path sandbox) throws IOException {
        directoryDependency = sandbox.resolve("dir").resolve(KNOWN_TYPES);
        fileDependency = sandbox.resolve(KNOWN_TYPES);
        archiveDependency = sandbox.resolve("zipped_descriptors.zip");
        writeResource("descriptors/dir/known_types.desc", directoryDependency);
        directoryDependency = directoryDependency.getParent();
        writeResource("descriptors/known_types.desc", fileDependency);
        writeResource("descriptors/zipped_descriptors.zip", archiveDependency);
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

    private static void writeResource(String resourceName, Path destination) throws IOException {
        File destinationFile = destination.toFile();
        createParentDirs(destinationFile);
        InputStream resource = FileDescriptorSupersetTest.class.getClassLoader()
                                                               .getResourceAsStream(resourceName);
        assertNotNull(resource);
        copy(resource, destination, REPLACE_EXISTING);
    }
}
