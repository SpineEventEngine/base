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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Any;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.test.code.proto.AnyWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(TempDirectory.class)
@DisplayName("FileSet should")
class FileSetTest {

    @Test
    @DisplayName("load mains resources")
    void loadMainResources() {
        assertFalse(FileSet.load()
                           .isEmpty());
    }

    @Test
    @DisplayName("parse using dependencies")
    void parseUsingDependencies(@TempDir Path tempDir) {
        File descriptorSetFile = tempDir.resolve("test.desc")
                                        .toFile();
        ImmutableSet<FileDescriptor> dependencies = ImmutableSet.of(Any.getDescriptor()
                                                                       .getFile());
        FileDescriptor dependant = AnyWrapper.getDescriptor()
                                             .getFile();
        writeDescriptorSet(descriptorSetFile, dependant.toProto());
        FileSet parsedFiles = FileSet.parse(descriptorSetFile, dependencies);
        assertThat(parsedFiles.size()).isEqualTo(1 + dependencies.size());
    }

    private static void writeDescriptorSet(File file, FileDescriptorProto... descriptors) {
        List<FileDescriptorProto> descriptorList = asList(descriptors);
        FileDescriptorSet descriptorSet = FileDescriptorSet
                .newBuilder()
                .addAllFile(descriptorList)
                .build();
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            descriptorSet.writeTo(out);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
