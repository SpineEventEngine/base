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

package io.spine.tools.fromjson;

import io.spine.code.proto.FileSet;
import io.spine.tools.fromjson.generator.FromJsonWriter;
import io.spine.tools.fromjson.generator.KnownTypeParsersWriter;
import io.spine.tools.fromjson.generator.KnownTypesWriter;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

class ProtoFromJsonWriter {

    private final Project project;
    private final FileSet protoJsFiles;

    private ProtoFromJsonWriter(Project project, FileSet protoJsFiles) {
        this.project = project;
        this.protoJsFiles = protoJsFiles;
    }

    static ProtoFromJsonWriter createFor(Project project) {
        FileSet protoJsFiles = collectProtoJsFiles(project);
        ProtoFromJsonWriter generator = new ProtoFromJsonWriter(project, protoJsFiles);
        return generator;
    }

    private static FileSet collectProtoJsFiles(Project project) {
        File descriptorSetFile = descriptorSetFile(project);
        if (descriptorSetFile.exists()) {
            FileSet fileSet = FileSet.parse(descriptorSetFile);
            return fileSet;
        }
        FileSet emptySet = FileSet.newInstance();
        return emptySet;
    }

    private static File descriptorSetFile(Project project) {
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        // todo distinguish between main and test sources
        Path path = Paths.get(absolutePath, "build", "descriptors", "test", "known_types.desc");
        File file = path.toFile();
        return file;
    }

    boolean hasMessagesToProcess() {
        boolean hasMessagesToProcess = !protoJsFiles.isEmpty();
        return hasMessagesToProcess;
    }

    void createFromJsonForProtos() {
        generateKnownTypes();
        generateKnownTypeParsers();
        insertFromJsonMethod();
    }

    private void generateKnownTypes() {
        KnownTypesWriter writer = KnownTypesWriter.createFor(project, protoJsFiles);
        writer.writeFile();
    }

    private void generateKnownTypeParsers() {
        KnownTypeParsersWriter writer = KnownTypeParsersWriter.createFor(project);
        writer.writeFile();
    }

    // todo add package-info everywhere.
    private void insertFromJsonMethod() {
        FromJsonWriter writer = new FromJsonWriter(project, protoJsFiles);
        writer.writeFromJsonIntoMessages();
    }
}
