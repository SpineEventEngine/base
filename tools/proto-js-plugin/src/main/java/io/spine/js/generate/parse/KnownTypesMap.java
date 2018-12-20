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

package io.spine.js.generate.parse;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.js.FileName;
import io.spine.code.js.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.CodeLine;
import io.spine.js.generate.CodeLines;
import io.spine.js.generate.RawLine;
import io.spine.js.generate.Snippet;
import io.spine.js.generate.importado.JsImportGenerator;
import io.spine.type.TypeUrl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static io.spine.js.generate.CodeLines.commaSeparated;
import static io.spine.js.generate.RawLine.emptyLine;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * The generator of the global known types {@code Map}.
 *
 * <p>This class generates the map with all the known types written in the form of
 * "{@linkplain TypeUrl type-url}-to-JS-type", as well as the imports necessary to use the types.
 */
public final class KnownTypesMap implements Snippet {

    /**
     * The exported map name.
     */
    private static final String MAP_NAME = "types";

    private final FileSet fileSet;

    /**
     * Creates a new {@code KnownTypesGenerator}.
     *
     * <p>All the known types will be acquired from the {@code fileSet}.
     *
     * @param fileSet
     *         the {@code FileSet} containing all the known types
     */
    public KnownTypesMap(FileSet fileSet) {
        this.fileSet = fileSet;
    }

    /**
     * Generates the known types code.
     *
     * <p>The code includes:
     * <ol>
     *     <li>Imports of all JS files declaring generated messages
     *     <li>The global {@code Map} of known types
     * </ol>
     */
    @Override
    public CodeLines value() {
        CodeLines snippet = new CodeLines();
        generateImports(snippet);
        snippet.append(generateKnownTypesMap());
        return snippet;
    }

    /**
     * Generates import statements for all files declaring generated messages.
     */
    @VisibleForTesting
    void generateImports(CodeLines output) {
        Collection<FileDescriptor> files = fileSet.files();
        Set<FileName> imports = files.stream()
                                     .filter(file -> !file.getMessageTypes()
                                                          .isEmpty())
                                     .map(FileName::from)
                                     .collect(toSet());
        JsImportGenerator generator = JsImportGenerator
                .newBuilder()
                .setImports(imports)
                .setJsOutput(output)
                .build();
        generator.generate();
    }

    /**
     * Generates the {@code Map} of known types.
     *
     * <p>Map entries are known types stored in the "{@linkplain TypeUrl type-url}-to-JS-type"
     * format.
     *
     * <p>The map is exported under the {@link #MAP_NAME}.
     */
    @VisibleForTesting
    CodeLines generateKnownTypesMap() {
        CodeLines snippet = new CodeLines();
        snippet.append(emptyLine());
        snippet.exportMap(MAP_NAME);
        storeKnownTypes(snippet);
        snippet.quitMapDeclaration();
        return snippet;
    }

    /**
     * Stores known types to the declared {@code Map}.
     */
    private void storeKnownTypes(CodeLines output) {
        Collection<FileDescriptor> files = fileSet.files();
        for (FileDescriptor file : files) {
            storeTypesFromFile(file, output);
        }
    }

    /**
     * Stores all message types declared in a file as known types {@code Map} entries.
     */
    private static void storeTypesFromFile(FileDescriptor file, CodeLines output) {
        List<Descriptor> messages = file.getMessageTypes();
        List<CodeLine> entries = messages.stream()
                                         .map(KnownTypesMap::mapEntry)
                                         .collect(toList());
        output.append(commaSeparated(entries));
    }

    /**
     * Obtains type URL and JS type name of the {@code message} and creates a {@code Map} entry of
     * the "{@linkplain TypeUrl type-url}-to-JS-type" format.
     */
    private static CodeLine mapEntry(Descriptor message) {
        TypeUrl typeUrl = TypeUrl.from(message);
        TypeName typeName = TypeName.from(message);
        CodeLine mapEntry = RawLine.mapEntry(typeUrl.value(), typeName);
        return mapEntry;
    }
}
