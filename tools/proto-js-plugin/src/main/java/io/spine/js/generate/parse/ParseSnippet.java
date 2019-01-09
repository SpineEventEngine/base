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
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.MessageType;
import io.spine.code.proto.TypeSet;
import io.spine.js.generate.Snippet;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.snippet.Comment;
import io.spine.js.generate.output.snippet.Import;

import static io.spine.js.generate.output.CodeLine.emptyLine;

/**
 * The code for parsing of a Protobuf message from a plain Javascript object.
 */
public final class ParseSnippet implements Snippet {

    /**
     * The name of the import of parsers registry.
     *
     * <p>Visible so the other generators such as a
     * {@linkplain io.spine.js.generate.field.FieldGenerator field} can use the import.
     */
    public static final String TYPE_PARSERS_IMPORT_NAME = "TypeParsers";
    /** The name of the {@code object_parser.js} import. */
    static final String ABSTRACT_PARSER_IMPORT_NAME = "ObjectParser";

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
    public ParseSnippet(FileDescriptor file) {
        this.file = file;
    }

    @Override
    public CodeLines value() {
        CodeLines out = new CodeLines();
        out.append(emptyLine());
        out.append(COMMENT);
        out.append(emptyLine());
        out.append(imports());
        out.append(parseCode());
        return out;
    }

    /**
     * Generates imports required by the code for parsing of messages.
     */
    @VisibleForTesting
    CodeLines imports() {
        String abstractParserImport = Import.libraryDefault(OBJECT_PARSER_FILE)
                                            .namedAs(ABSTRACT_PARSER_IMPORT_NAME);
        String parsers2Import = Import.libraryDefault(TYPE_PARSERS_FILE)
                                      .namedAs(TYPE_PARSERS_IMPORT_NAME);
        CodeLines lines = new CodeLines();
        lines.append(abstractParserImport);
        lines.append(parsers2Import);
        return lines;
    }

    @VisibleForTesting
    CodeLines parseCode() {
        CodeLines snippet = new CodeLines();
        for (MessageType message : TypeSet.onlyMessages(file)) {
            GeneratedParser parser = new GeneratedParser(message.descriptor());
            snippet.append(emptyLine());
            snippet.append(parser);
        }
        return snippet;
    }
}
