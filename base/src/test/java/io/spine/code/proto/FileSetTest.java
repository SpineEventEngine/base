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

package io.spine.code.proto;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.Correspondence;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Empty;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.test.code.proto.MessageDecl;
import io.spine.type.MessageType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("`FileSet` should")
class FileSetTest {

    private static final Correspondence<FileDescriptor, String> fileNames = Correspondence.from(
            (@NonNull FileDescriptor file, @NonNull String name) -> file.getFullName().equals(name),
            "has name"
    );

    private FileSet fileSet;
    private Path tempDir;

    @BeforeEach
    void load(@TempDir Path tempDir) {
        this.fileSet = FileSet.load();
        this.tempDir = tempDir;
    }

    @Test
    @DisplayName("load mains resources")
    void loadMainResources() {
        assertFalse(fileSet.isEmpty());
    }

    @Test
    @DisplayName("return all declared top-level messages")
    void returnTopLevelMessages() {
        ImmutableSet<FileName> fileNames =
                ImmutableSet.of(FileName.of("spine/test/code/proto/file_set_test.proto"));
        FileSet set = fileSet.find(fileNames);
        List<MessageType> types = set.topLevelMessages();
        assertThat(types).hasSize(1);

        MessageType onlyElement = types.get(0);
        assertThat(onlyElement.javaClass()).isEqualTo(MessageDecl.class);
    }

    @Test
    @DisplayName("filter message type by predicate")
    void findType() {
        String nameFragment = "Field";
        List<MessageType> types =
                fileSet.findMessageTypes((d) -> d.getName()
                                                 .contains(nameFragment));
        types.forEach(
                type -> assertThat(type.name().value()).contains(nameFragment)
        );
    }

    @Test
    @DisplayName("load from known types")
    void loadFromKnownTypes() throws IOException {
        File file = writeToFile(Empty.getDescriptor()
                                     .getFile()
                                     .toProto());
        FileSet set = FileSet.parseAsKnownFiles(file);
        assertThat(set.files())
                .comparingElementsUsing(fileNames)
                .containsExactly("google/protobuf/empty.proto");
    }

    @Test
    @DisplayName("load from known types and ignore unknown")
    void ignoreUnknown() throws IOException {
        FileDescriptorProto unknownFile = FileDescriptorProto
                .newBuilder()
                .setName("example/definition/unknown_file.proto")
                .build();
        File file = writeToFile(unknownFile);
        FileSet set = FileSet.parseAsKnownFiles(file);
        assertThat(set.files())
                .isEmpty();
    }

    private File writeToFile(FileDescriptorProto fileDescriptor) throws IOException {
        FileDescriptorSet descriptorSet = FileDescriptorSet
                .newBuilder()
                .addFile(fileDescriptor)
                .build();
        File file = new File(tempDir.toString(), "temp.desc");
        file.createNewFile();
        try (FileOutputStream output = new FileOutputStream(file)) {
            descriptorSet.writeTo(output);
        }
        return file;
    }
}
