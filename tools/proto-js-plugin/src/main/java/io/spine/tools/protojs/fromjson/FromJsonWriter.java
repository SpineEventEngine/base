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

package io.spine.tools.protojs.fromjson;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsOutput;
import io.spine.tools.protojs.code.JsWriter;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;

@SuppressWarnings({"DuplicateStringLiteralInspection", "MethodMayBeStatic"}) // todo get rid.
public class FromJsonWriter {

    private final Project project;
    private final FileSet protoJsFiles;

    public FromJsonWriter(Project project, FileSet protoJsFiles) {
        this.project = project;
        this.protoJsFiles = protoJsFiles;
    }

    public void writeFromJsonIntoMessages() {
        Path protoJsFolder = getProtoJsLocation(project);
        for (FileDescriptor fileDescriptor : protoJsFiles.files()) {
            Path jsFilePath = jsFilePath(fileDescriptor, protoJsFolder);
            writeIntoFile(fileDescriptor, jsFilePath);
        }
    }

    private void writeIntoFile(FileDescriptor fileDescriptor, Path jsFilePath) {
        if (!Files.exists(jsFilePath)) {
            return;
        }
        JsWriter jsWriter = new JsWriter();
        FromJsonGenerator generator = new FromJsonGenerator(fileDescriptor, jsWriter);
        generator.generateJs();
        JsOutput codeToWrite = jsWriter.getGeneratedCode();
        writeToFile(jsFilePath, codeToWrite);
    }

    // todo distinguish between main and test somewhere
    private static Path getProtoJsLocation(Project project) {
        File projectDir = project.getProjectDir();
        String projectAbsolutePath = projectDir.getAbsolutePath();
        Path protoJsLocation = Paths.get(projectAbsolutePath, "proto", "test", "js");
        return protoJsLocation;
    }

    private static Path jsFilePath(FileDescriptor fileDescriptor, Path protoJsFolder) {
        FileName fileName = FileName.from(fileDescriptor);
        String nameWithoutExtension = fileName.nameWithoutExtension();
        Path fullPathWithoutExtension = Paths.get(protoJsFolder.toString(), nameWithoutExtension);
        String jsFilePath = fullPathWithoutExtension + "_pb.js";
        Path result = Paths.get(jsFilePath);
        return result;
    }

    // todo remove copypaste
    private static void writeToFile(Path path, JsOutput output) {
        try {
            String content = output.toString();
            Files.write(path, content.getBytes(), APPEND);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
