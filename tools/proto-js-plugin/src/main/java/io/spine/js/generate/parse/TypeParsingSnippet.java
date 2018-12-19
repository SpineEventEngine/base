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
import io.spine.js.generate.CodeSnippet;
import io.spine.js.generate.JsOutput;
import io.spine.js.generate.importado.JsImportGenerator;

import static io.spine.code.js.LibraryFile.KNOWN_TYPE_PARSERS;

/**
 * The generator of the {@code fromJson(json)} method for the given {@link FileDescriptor}.
 *
 * <p>The class generates the {@code fromJson} and {@code fromObject} methods for each message
 * declared in the {@link FileDescriptor}.
 */
public final class TypeParsingSnippet implements CodeSnippet {

    /**
     * The name of the {@code known_type_parsers.js} import.
     *
     * <p>Visible so the other generators such as {@linkplain FromJsonMethod message} or
     * {@linkplain io.spine.js.generate.parse.field.FieldGenerator field} can use the import.
     */
    public static final String PARSERS_IMPORT_NAME = "known_type_parsers";

    /**
     * The comment inserted before the generated code.
     */
    @VisibleForTesting
    static final String COMMENT =
            "The code for parsing the Protobuf messages of this file from the JSON data.";

    private final FileDescriptor file;

    /**
     * Creates the new {@code FileGenerator} which will process the given file descriptor.
     *
     * @param file
     *         the {@code FileDescriptor} whose messages to process
     */
    public TypeParsingSnippet(FileDescriptor file) {
        this.file = file;
    }

    /**
     * Generates the {@code fromJson(json)} method and all the related code for each message of the
     * processed {@code file}.
     *
     * <p>More specifically:
     * <ol>
     *     <li>Writes a comment explaining the generated code.
     *     <li>Adds an import for the standard Protobuf type parsers.
     *     <li>For each message, adds the {@code fromJson(json)} method which parses JSON
     *         {@code string} into object.
     *     <li>For each message, adds the {@code fromObject(obj)} method which parses the JS object
     *         and creates a message instance.
     * </ol>
     */
    @Override
    public JsOutput value() {
        JsOutput out = new JsOutput();
        out.addLinesFrom(generateComment());
        out.addLinesFrom(generateParsersImport());
        out.addLinesFrom(generateMethods());
        return out;
    }

    /**
     * Generates comment explaining the generated code.
     */
    @VisibleForTesting
    JsOutput generateComment() {
        JsOutput comment = new JsOutput();
        comment.addEmptyLine();
        comment.addComment(COMMENT);
        return comment;
    }

    /**
     * Generates the {@code known_type_parsers.js} import.
     *
     * <p>The import path is relative to the processed {@code file}.
     */
    @VisibleForTesting
    JsOutput generateParsersImport() {
        JsOutput snippet = new JsOutput();
        snippet.addEmptyLine();
        FileName fileName = FileName.from(file);
        JsImportGenerator generator = JsImportGenerator
                .newBuilder()
                .setFileName(fileName)
                .setJsOutput(snippet)
                .build();
        generator.importFile(KNOWN_TYPE_PARSERS.fileName(), PARSERS_IMPORT_NAME);
        return snippet;
    }

    /**
     * Generates the {@code fromJson(json)} and {@code fromObject(obj)} methods for each message of
     * the file.
     */
    @VisibleForTesting
    JsOutput generateMethods() {
        JsOutput snippet = new JsOutput();
        for (Descriptor message : file.getMessageTypes()) {
            FromJsonMethod generator = FromJsonMethod.createFor(message, snippet);
            generator.generate();
        }
        return snippet;
    }
}
