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

package io.spine.tools.protojs;

import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.fromjson.FromJsonWriter;
import io.spine.tools.protojs.knowntypes.KnownTypeParsersWriter;
import io.spine.tools.protojs.knowntypes.KnownTypesWriter;

import java.io.File;
import java.nio.file.Path;

class ProtoFromJsonWriter {

    private final Path protoJsLocation;
    private final FileSet protoJsFiles;

    private ProtoFromJsonWriter(Path protoJsLocation, FileSet protoJsFiles) {
        this.protoJsLocation = protoJsLocation;
        this.protoJsFiles = protoJsFiles;
    }

    static ProtoFromJsonWriter createFor(Path protoJsLocation, File descriptorSetFile) {
        FileSet protoJsFiles = collectProtoJsFiles(descriptorSetFile);
        return new ProtoFromJsonWriter(protoJsLocation, protoJsFiles);
    }

    boolean hasFilesToProcess() {
        boolean hasFilesToProcess = !protoJsFiles.isEmpty();
        return hasFilesToProcess;
    }

    void writeFromJsonForProtos() {
        writeKnownTypes();
        writeKnownTypeParsers();
        writeFromJsonMethod();
    }

    private void writeKnownTypes() {
        KnownTypesWriter writer = KnownTypesWriter.createFor(protoJsLocation, protoJsFiles);
        writer.writeFile();
    }

    private void writeKnownTypeParsers() {
        KnownTypeParsersWriter writer = KnownTypeParsersWriter.createFor(protoJsLocation);
        writer.writeFile();
    }

    // todo add package-info everywhere.
    // todo fix all warnings.
    private void writeFromJsonMethod() {
        FromJsonWriter writer = new FromJsonWriter(protoJsLocation, protoJsFiles);
        writer.writeIntoFiles();
    }

    private static FileSet collectProtoJsFiles(File descriptorSetFile) {
        if (descriptorSetFile.exists()) {
            FileSet fileSet = FileSet.parse(descriptorSetFile);
            return fileSet;
        }
        FileSet emptySet = FileSet.newInstance();
        return emptySet;
    }
}
