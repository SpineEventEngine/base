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
import io.spine.tools.fromjson.generator.FromJsonInserter;
import io.spine.tools.fromjson.generator.KnownTypeParsersGenerator;
import io.spine.tools.fromjson.generator.KnownTypesJsGenerator;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.gradle.internal.impldep.org.apache.commons.lang.CharEncoding.UTF_8;

class ProtoFromJsonGenerator {

    private final Project project;
    private final FileSet protoJsFiles;

    private ProtoFromJsonGenerator(Project project, FileSet protoJsFiles) {
        this.project = project;
        this.protoJsFiles = protoJsFiles;
    }

    static ProtoFromJsonGenerator createFor(Project project) {
        FileSet protoJsFiles = collectProtoJsFiles(project);
        ProtoFromJsonGenerator generator = new ProtoFromJsonGenerator(project, protoJsFiles);
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
        KnownTypesJsGenerator generator = new KnownTypesJsGenerator(project, protoJsFiles);
        Path path = generator.composeFilePath();
        String content = generator.createFileContent();
        writeToFile(path, content);
    }

    private void generateKnownTypeParsers() {
        KnownTypeParsersGenerator generator = new KnownTypeParsersGenerator(project);
        Path path = generator.composeFilePath();
        String content = generator.createFileContent();
        writeToFile(path, content);
    }

    private void insertFromJsonMethod() {
        FromJsonInserter inserter = new FromJsonInserter(project, protoJsFiles);
        inserter.insertFromJsonIntoMessages();
    }

    private static void writeToFile(Path path, String content) {
        try {
            Files.write(path, content.getBytes(UTF_8), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
