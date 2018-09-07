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
import io.spine.tools.protojs.code.JsImportGenerator;
import io.spine.tools.protojs.code.JsWriter;
import io.spine.type.TypeUrl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPES;
import static io.spine.tools.protojs.files.JsFiles.jsFileName;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

class KnownTypesGenerator {

    private static final String MAP_NAME = "types";

    private final FileSet protoJsFiles;
    private final JsWriter jsWriter;

    KnownTypesGenerator(FileSet protoJsFiles, JsWriter jsWriter) {
        this.protoJsFiles = protoJsFiles;
        this.jsWriter = jsWriter;
    }

    void generateKnownTypes() {
        generateImports();
        jsWriter.addEmptyLine();
        generateKnownTypesMap();
    }

    // todo make methods shorter if necessary
    private void generateImports() {
        Collection<FileDescriptor> fileDescriptors = protoJsFiles.getFileDescriptors();
        JsImportGenerator importGenerator = JsImportGenerator.createFor(KNOWN_TYPES);
        for (FileDescriptor fileDescriptor : fileDescriptors) {
            generateImport(importGenerator, fileDescriptor);
        }
    }

    private void generateImport(JsImportGenerator importGenerator, FileDescriptor fileDescriptor) {
        String fileToImport = jsFileName(fileDescriptor);
        List<Descriptor> declaredMessages = fileDescriptor.getMessageTypes();
        if (declaredMessages.size() > 0) {
            String statement = importGenerator.createImport(fileToImport);
            jsWriter.addLine(statement);
        }
    }

    private void generateKnownTypesMap() {
        jsWriter.addLine("export const " + MAP_NAME + " = new Map([");
        jsWriter.increaseDepth();
        storeKnownTypes();
        jsWriter.decreaseDepth();
        jsWriter.addLine("]);");
    }

    private void storeKnownTypes() {
        Collection<FileDescriptor> fileDescriptors = protoJsFiles.getFileDescriptors();
        for (Iterator<FileDescriptor> it = fileDescriptors.iterator(); it.hasNext(); ) {
            FileDescriptor fileDescriptor = it.next();
            boolean isLastFile = !it.hasNext();
            addTypesToMap(fileDescriptor, isLastFile);
        }
    }

    private void addTypesToMap(FileDescriptor fileDescriptor, boolean isLastFile) {
        List<Descriptor> messages = fileDescriptor.getMessageTypes();
        for (Iterator<Descriptor> it = messages.iterator(); it.hasNext(); ) {
            Descriptor descriptor = it.next();
            boolean isLastMessage = !it.hasNext() && isLastFile;
            addMapEntry(descriptor, isLastMessage);
        }
    }

    private void addMapEntry(Descriptor descriptor, boolean isLastMessage) {
        String mapEntry = jsMapEntry(descriptor);
        String entryToAdd = appendCommaIfNecessary(mapEntry, isLastMessage);
        jsWriter.addLine(entryToAdd);
    }

    private static String jsMapEntry(Descriptor descriptor) {
        TypeUrl typeUrl = TypeUrl.from(descriptor);
        String typeName = typeWithProtoPrefix(descriptor);
        String mapEntry = "['" + typeUrl + "', " + typeName + ']';
        return mapEntry;
    }

    private static String appendCommaIfNecessary(String mapEntry, boolean isLastMessage) {
        StringBuilder mapEntryBuilder = new StringBuilder(mapEntry);
        if (!isLastMessage) {
            mapEntryBuilder.append(',');
        }
        return mapEntryBuilder.toString();
    }
}
