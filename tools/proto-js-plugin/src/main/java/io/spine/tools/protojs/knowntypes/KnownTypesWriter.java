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

package io.spine.tools.protojs.knowntypes;

import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsOutput;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPES;
import static io.spine.tools.protojs.files.JsFiles.writeToFile;

public final class KnownTypesWriter {

    private final Path filePath;
    private final FileSet protoJsFiles;

    private KnownTypesWriter(Path filePath, FileSet protoJsFiles) {
        this.filePath = filePath;
        this.protoJsFiles = protoJsFiles;
    }

    public static KnownTypesWriter createFor(Path protoJsLocation, FileSet protoJsFiles) {
        checkNotNull(protoJsLocation);
        checkNotNull(protoJsFiles);
        Path path = composeFilePath(protoJsLocation);
        return new KnownTypesWriter(path, protoJsFiles);
    }

    public void writeFile() {
        int indent = 4;
        JsGenerator jsGenerator = new JsGenerator(indent);
        KnownTypesGenerator generator = new KnownTypesGenerator(protoJsFiles, jsGenerator);
        generator.generateJs();
        JsOutput generatedCode = jsGenerator.getGeneratedCode();
        writeToFile(filePath, generatedCode);
    }

    private static Path composeFilePath(Path protoJsLocation) {
        Path path = Paths.get(protoJsLocation.toString(), KNOWN_TYPES);
        return path;
    }
}
