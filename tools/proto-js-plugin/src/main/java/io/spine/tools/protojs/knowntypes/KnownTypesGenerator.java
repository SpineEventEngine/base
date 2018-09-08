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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsImportGenerator;
import io.spine.type.TypeUrl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPES;
import static io.spine.tools.protojs.files.JsFiles.jsFileName;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

final class KnownTypesGenerator {

    private static final String MAP_NAME = "types";

    private final FileSet protoJsFiles;
    private final JsGenerator jsGenerator;

    KnownTypesGenerator(FileSet protoJsFiles, JsGenerator jsGenerator) {
        this.protoJsFiles = protoJsFiles;
        this.jsGenerator = jsGenerator;
    }

    void generateJs() {
        generateImports();
        jsGenerator.addEmptyLine();
        generateKnownTypesMap();
    }

    private void generateImports() {
        Collection<FileDescriptor> files = protoJsFiles.getFileDescriptors();
        JsImportGenerator importGenerator = JsImportGenerator.createFor(KNOWN_TYPES);
        for (FileDescriptor file : files) {
            generateImport(importGenerator, file);
        }
    }

    private void generateImport(JsImportGenerator importGenerator, FileDescriptor file) {
        String jsFileName = jsFileName(file);
        List<Descriptor> declaredMessages = file.getMessageTypes();
        if (declaredMessages.size() > 0) {
            String statement = importGenerator.importStatement(jsFileName);
            jsGenerator.addLine(statement);
        }
    }

    private void generateKnownTypesMap() {
        jsGenerator.exportMap(MAP_NAME);
        storeKnownTypes();
        jsGenerator.quitMapDeclaration();
    }

    private void storeKnownTypes() {
        Collection<FileDescriptor> files = protoJsFiles.getFileDescriptors();
        for (Iterator<FileDescriptor> it = files.iterator(); it.hasNext(); ) {
            FileDescriptor file = it.next();
            boolean isLastFile = !it.hasNext();
            storeTypesFromFile(file, isLastFile);
        }
    }

    private void storeTypesFromFile(FileDescriptor file, boolean isLastFile) {
        List<Descriptor> messages = file.getMessageTypes();
        for (Iterator<Descriptor> it = messages.iterator(); it.hasNext(); ) {
            Descriptor message = it.next();
            boolean isLastMessage = !it.hasNext() && isLastFile;
            addMapEntry(message, isLastMessage);
        }
    }

    private void addMapEntry(Descriptor message, boolean isLastMessage) {
        String mapEntry = jsMapEntry(message);
        jsGenerator.addMapEntry(mapEntry, isLastMessage);
    }

    private static String jsMapEntry(Descriptor message) {
        TypeUrl typeUrl = TypeUrl.from(message);
        String typeName = typeWithProtoPrefix(message);
        String mapEntry = "['" + typeUrl + "', " + typeName + ']';
        return mapEntry;
    }
}
