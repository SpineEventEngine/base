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
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsImportGenerator;
import io.spine.tools.protojs.code.JsWriter;
import io.spine.type.TypeUrl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPES;

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
    // todo have common file with file paths
    private void generateImports() {
        Set<Entry<FileName, FileDescriptor>> entries = protoJsFiles.getEntries();
        JsImportGenerator importGenerator = JsImportGenerator.createFor(KNOWN_TYPES);
        for (Entry<FileName, FileDescriptor> entry : entries) {
            FileName fileName = entry.getKey();
            FileDescriptor fileDescriptor = entry.getValue();
            String nameWithoutExtension = fileName.nameWithoutExtension();
            String fileToImport = nameWithoutExtension + "_pb.js";
            List<Descriptor> declaredMessages = fileDescriptor.getMessageTypes();
            int declaredMessagesCount = declaredMessages.size();
            if (declaredMessagesCount > 0) {
                String statement = importGenerator.createImport(fileToImport);
                jsWriter.addLine(statement);
            }
        }
    }

    private void generateKnownTypesMap() {
        jsWriter.addLine("export const " + MAP_NAME + " = new Map([");
        jsWriter.increaseDepth();

        Collection<FileDescriptor> fileDescriptors = protoJsFiles.getFileDescriptors();
        for (Iterator<FileDescriptor> it = fileDescriptors.iterator(); it.hasNext(); ) {
            FileDescriptor fileDescriptor = it.next();
            List<Descriptor> declaredMessages = fileDescriptor.getMessageTypes();
            boolean isLastFile = !it.hasNext();
            addTypesToMap(declaredMessages, isLastFile);
        }

        jsWriter.decreaseDepth();
        jsWriter.addLine("]);");
    }

    private void addTypesToMap(Iterable<Descriptor> messages, boolean isLastFile) {
        for (Iterator<Descriptor> it = messages.iterator(); it.hasNext(); ) {
            Descriptor descriptor = it.next();
            String mapEntry = createMapEntry(descriptor);
            StringBuilder mapEntryBuilder = new StringBuilder(mapEntry);
            boolean isLastMessage = !it.hasNext();
            if (!isLastMessage || !isLastFile) {
                mapEntryBuilder.append(',');
            }
            String line = mapEntryBuilder.toString();
            jsWriter.addLine(line);
        }
    }

    private static String createMapEntry(Descriptor descriptor) {
        TypeUrl typeUrl = TypeUrl.from(descriptor);
        String messageName = descriptor.getFullName();
        String nameWithProtoPrefix = "proto." + messageName;
        String mapEntry = "['" + typeUrl + "', " + nameWithProtoPrefix + ']';
        return mapEntry;
    }
}
