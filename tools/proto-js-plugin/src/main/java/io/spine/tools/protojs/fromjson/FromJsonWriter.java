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
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsOutput;
import io.spine.tools.protojs.code.JsWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.tools.protojs.files.JsFiles.appendToFile;
import static io.spine.tools.protojs.files.JsFiles.jsFileName;

public class FromJsonWriter {

    private final Path protoJsLocation;
    private final FileSet protoJsFiles;

    public FromJsonWriter(Path protoJsLocation, FileSet protoJsFiles) {
        this.protoJsLocation = protoJsLocation;
        this.protoJsFiles = protoJsFiles;
    }

    public void writeFromJsonIntoMessages() {
        for (FileDescriptor fileDescriptor : protoJsFiles.files()) {
            Path jsFilePath = composeFilePath(fileDescriptor);
            writeIntoFile(fileDescriptor, jsFilePath);
        }
    }

    private Path composeFilePath(FileDescriptor fileDescriptor) {
        String jsFileName = jsFileName(fileDescriptor);
        Path filePath = Paths.get(protoJsLocation.toString(), jsFileName);
        return filePath;
    }

    private static void writeIntoFile(FileDescriptor fileDescriptor, Path jsFilePath) {
        if (!Files.exists(jsFilePath)) {
            return;
        }
        JsWriter jsWriter = new JsWriter();
        FromJsonGenerator generator = new FromJsonGenerator(fileDescriptor, jsWriter);
        generator.generateJs();
        JsOutput codeToWrite = jsWriter.getGeneratedCode();
        appendToFile(jsFilePath, codeToWrite);
    }
}
