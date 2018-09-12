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

import com.google.common.annotations.VisibleForTesting;
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

/**
 * The generator of the global known types JS {@code Map}.
 *
 * <p>This class generates the map with all the known types written in the form of
 * "{@linkplain TypeUrl type-url}-to-JS-type", as well as the imports necessary to use JS types.
 *
 * <p>All the generated code is stored to the {@link JsGenerator} provided on construction.
 *
 * @author Dmytro Kuzmin
 * @see KnownTypesWriter
 */
final class KnownTypesGenerator {

    /**
     * The exported map name.
     */
    private static final String MAP_NAME = "types";

    private final FileSet fileSet;
    private final JsGenerator jsGenerator;

    /**
     * Creates a new {@code KnownTypesGenerator}.
     *
     * <p>All the known types will be acquired from the {@code fileSet} and the {@code jsGenerator}
     * creates and accumulates JS code lines.
     *
     * @param fileSet
     *         the {@code FileSet} containing all the known types
     * @param jsGenerator
     *         the JS code generator to append the code to
     */
    KnownTypesGenerator(FileSet fileSet, JsGenerator jsGenerator) {
        this.fileSet = fileSet;
        this.jsGenerator = jsGenerator;
    }

    /**
     * Generates known types code.
     *
     * <p>The code includes:
     * <ol>
     *     <li>Imports of all files declaring Proto JS messages
     *     <li>Known types map itself
     * </ol>
     *
     * <p>The generated code is accumulated in the {@link #jsGenerator}.
     */
    void generateJs() {
        generateImports();
        generateKnownTypesMap();
    }

    /**
     * Generates import statements for all files declaring Proto JS messages.
     *
     * <p>Imports are written in the CommonJS style ({@code "require('./lib')"}).
     */
    @VisibleForTesting
    void generateImports() {
        Collection<FileDescriptor> files = fileSet.getFileDescriptors();
        JsImportGenerator importGenerator = JsImportGenerator.createFor(KNOWN_TYPES);
        for (FileDescriptor file : files) {
            generateImport(importGenerator, file);
        }
    }

    /**
     * Generates the JS {@code Map} of known types.
     *
     * <p>Map entries are known types stored in the "{@linkplain TypeUrl type-url}-to-JS-type
     * format.
     *
     * <p>The map is exported under the {@link #MAP_NAME}.
     */
    @VisibleForTesting
    void generateKnownTypesMap() {
        jsGenerator.addEmptyLine();
        jsGenerator.exportMap(MAP_NAME);
        storeKnownTypes();
        jsGenerator.quitMapDeclaration();
    }

    private void generateImport(JsImportGenerator importGenerator, FileDescriptor file) {
        String jsFileName = jsFileName(file);
        List<Descriptor> declaredMessages = file.getMessageTypes();
        if (!declaredMessages.isEmpty()) {
            String statement = importGenerator.importStatement(jsFileName);
            jsGenerator.addLine(statement);
        }
    }

    private void storeKnownTypes() {
        Collection<FileDescriptor> files = fileSet.getFileDescriptors();
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
