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
import io.spine.js.generate.Snippet;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.snippet.Comment;
import io.spine.js.generate.output.snippet.Import;

import static io.spine.code.js.LibraryFile.KNOWN_TYPE_PARSERS;
import static io.spine.js.generate.output.CodeLine.emptyLine;

/**
 * The generator of the {@code fromJson(json)} method for the given {@link FileDescriptor}.
 *
 * <p>The class generates the {@code fromJson} and {@code fromObject} methods for each message
 * declared in the {@link FileDescriptor}.
 */
public final class ParseMethodsSnippet implements Snippet {

    /**
     * The name of the {@code known_type_parsers.js} import.
     *
     * <p>Visible so the other generators such as {@linkplain FromJsonMethod message} or
     * {@linkplain io.spine.js.generate.field.FieldGenerator field} can use the import.
     */
    public static final String PARSERS_IMPORT_NAME = "known_type_parsers";
    /** The name of the {@code object_parser.js} import. */
    static final String ABSTRACT_PARSER_IMPORT_NAME = "ObjectParser";
    static final String TYPE_PARSERS_IMPORT_NAME = "TypeParsers";

    @VisibleForTesting
    static final String TYPE_PARSERS_FILE = "spine-web-client/client/parser/type-parsers.js";
    @VisibleForTesting
    static final String OBJECT_PARSER_FILE = "spine-web-client/client/parser/object-parser.js";

    /**
     * The comment inserted before the generated code.
     */
    @VisibleForTesting
    static final Comment COMMENT =
            Comment.of("The generated code for parsing the Protobuf messages from the JSON data.");

    private final FileDescriptor file;

    /**
     * Creates the new instance for the given file descriptor.
     *
     * @param file
     *         the {@code FileDescriptor} whose messages to process
     */
    public ParseMethodsSnippet(FileDescriptor file) {
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
    public CodeLines value() {
        CodeLines out = new CodeLines();
        out.append(emptyLine());
        out.append(COMMENT);
        out.append(emptyLine());
        out.append(imports());
        out.append(parseMethods());
        return out;
    }

    /**
     * Generates the {@code known_type_parsers.js} and {@code object_parser.js} imports.
     *
     * <p>The import path is relative to the processed {@code file}.
     */
    @VisibleForTesting
    CodeLines imports() {
        FileName fileName = FileName.from(file);
        String parsersImport = Import.fileRelativeTo(KNOWN_TYPE_PARSERS.fileName(), fileName)
                                     .namedAs(PARSERS_IMPORT_NAME);
        String abstractParserImport = Import.libraryDefault(OBJECT_PARSER_FILE)
                                            .namedAs(ABSTRACT_PARSER_IMPORT_NAME);
        String parsers2Import = Import.libraryDefault(TYPE_PARSERS_FILE)
                                      .namedAs(TYPE_PARSERS_IMPORT_NAME);
        CodeLines lines = new CodeLines();
        lines.append(parsersImport);
        lines.append(abstractParserImport);
        lines.append(parsers2Import);
        return lines;
    }

    /**
     * Generates the {@code fromJson(json)} and {@code fromObject(obj)} methods for each message of
     * the file.
     */
    @VisibleForTesting
    CodeLines parseMethods() {
        CodeLines snippet = new CodeLines();
        for (Descriptor message : file.getMessageTypes()) {
            FromJsonMethod fromJsonMethod = FromJsonMethod.createFor(message);
            snippet.append(emptyLine());
            snippet.append(fromJsonMethod);
        }
        return snippet;
    }
}
