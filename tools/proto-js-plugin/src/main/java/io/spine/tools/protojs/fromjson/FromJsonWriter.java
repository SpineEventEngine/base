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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsOutput;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.files.JsFiles.appendToFile;
import static io.spine.tools.protojs.files.JsFiles.jsFileName;
import static io.spine.type.TypeUrl.GOOGLE_PROTOBUF_PACKAGE;

public final class FromJsonWriter {

    private static final String SPINE_OPTIONS_PROTO = "spine/options.proto";

    private final Path protoJsLocation;
    private final FileSet protoJsFiles;

    private FromJsonWriter(Path protoJsLocation, FileSet protoJsFiles) {
        this.protoJsLocation = protoJsLocation;
        this.protoJsFiles = protoJsFiles;
    }

    public static FromJsonWriter createFor(Path protoJsLocation, FileSet protoJsFiles) {
        checkNotNull(protoJsLocation);
        checkNotNull(protoJsFiles);
        return new FromJsonWriter(protoJsLocation, protoJsFiles);
    }

    public void writeIntoFiles() {
        for (FileDescriptor file : protoJsFiles.files()) {
            if (!isStandardOrSpineOptions(file)) {
                Path jsFilePath = composeFilePath(file);
                writeIntoFile(file, jsFilePath);
            }
        }
    }

    private Path composeFilePath(FileDescriptor file) {
        String jsFileName = jsFileName(file);
        Path path = Paths.get(protoJsLocation.toString(), jsFileName);
        return path;
    }

    @VisibleForTesting
    public static boolean isStandardOrSpineOptions(FileDescriptor file) {
        boolean isStandardType = file.getPackage()
                                     .startsWith(GOOGLE_PROTOBUF_PACKAGE);
        boolean isSpineOptions = SPINE_OPTIONS_PROTO.equals(file.getFullName());
        return isStandardType || isSpineOptions;
    }

    private static void writeIntoFile(FileDescriptor file, Path filePath) {
        if (!Files.exists(filePath)) {
            return;
        }
        JsGenerator jsGenerator = new JsGenerator();
        FromJsonGenerator generator = new FromJsonGenerator(file, jsGenerator);
        generator.generateJs();
        JsOutput generatedCode = jsGenerator.getGeneratedCode();
        appendToFile(filePath, generatedCode);
    }
}
