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

package io.spine.tools.protojs.fromjson;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsImportGenerator;
import io.spine.tools.protojs.message.MessageHandler;

import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPE_PARSERS;

/**
 * The generator of the {@code fromJson(json)} method for the JS Proto definitions.
 *
 * <p>The class generates the {@code fromJson} method for each message declared in the
 * {@link FileDescriptor}.
 *
 * <p>All the generated code is stored in the given {@link JsGenerator}.
 *
 * @apiNote
 * Like the other handlers and generators of this module, the {@code FromJsonGenerator} is meant to
 * operate on the common {@link io.spine.tools.protojs.code.JsGenerator} passed on construction and
 * thus its methods do not return any generated code.
 *
 * @author Dmytro Kuzmin
 */
public final class FromJsonGenerator {

    /**
     * The name of the {@code known_type_parsers.js} import.
     */
    public static final String PARSERS_IMPORT_NAME = "known_type_parsers";

    /**
     * The comment inserted before the generated code.
     */
    @VisibleForTesting
    static final String COMMENT = "The code for parsing the Protobuf messages of this file from " +
            "the JSON data.";

    private final FileDescriptor file;
    private final JsGenerator jsGenerator;

    /**
     * Creates the new {@code FromJsonGenerator} which will process the given file descriptor.
     *
     * <p>Passed {@code JsGenerator} is used to create and accumulate the JS code lines.
     *
     * @param file
     *         the {@code FileDescriptor} whose messages to process
     * @param jsGenerator
     *         the {@code JsGenerator} to accumulate the JS code
     */
    FromJsonGenerator(FileDescriptor file, JsGenerator jsGenerator) {
        this.file = file;
        this.jsGenerator = jsGenerator;
    }

    /**
     * Generates the {@code fromJson(json)} method and all the related code for each message of the
     * processed {@link #file}.
     *
     * <p>More specifically:
     * <ol>
     *     <li>Writes a comment explaining the generated code.
     *     <li>Adds an import for the
     *         {@linkplain io.spine.tools.protojs.knowntypes.KnownTypeParsersWriter known parsers}.
     *     <li>Adds the {@code fromJson(json)} method for each message which parses JSON string
     *         into object.
     *     <li>Adds the {@code fromObject(obj)} method for each message which parses the JS object
     *         and creates a message.
     * </ol>
     */
    void generateJs() {
        generateComment();
        generateParsersImport();
        generateMethods();
    }

    /**
     * Generates comment explaining the generated code.
     */
    @VisibleForTesting
    void generateComment() {
        jsGenerator.addEmptyLine();
        jsGenerator.addComment(COMMENT);
    }

    /**
     * Generates the {@link io.spine.tools.protojs.knowntypes.KnownTypeParsersWriter
     * known_type_parsers.js} import.
     *
     * <p>The import path is relative to the stored {@link #file}.
     */
    @VisibleForTesting
    void generateParsersImport() {
        jsGenerator.addEmptyLine();
        String fileName = file.getFullName();
        JsImportGenerator generator = JsImportGenerator.createFor(fileName);
        String parsersImport = generator.namedImport(KNOWN_TYPE_PARSERS, PARSERS_IMPORT_NAME);
        jsGenerator.addLine(parsersImport);
    }

    /**
     * Generates the {@code fromJson(json)} and {@code fromObject(obj)} methods.
     */
    @VisibleForTesting
    void generateMethods() {
        for (Descriptor message : file.getMessageTypes()) {
            MessageHandler handler = MessageHandler.createFor(message, jsGenerator);
            handler.generateJs();
        }
    }
}
