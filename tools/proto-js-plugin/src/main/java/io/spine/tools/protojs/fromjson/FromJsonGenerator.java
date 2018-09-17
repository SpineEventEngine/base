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
import io.spine.tools.protojs.generate.JsImportGenerator;
import io.spine.tools.protojs.generate.JsOutput;
import io.spine.tools.protojs.message.MessageHandler;

import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPE_PARSERS;

/**
 * The generator of the {@code fromJson(json)} method for the JS Proto definitions.
 *
 * <p>The class generates the {@code fromJson} method for each message declared in the
 * {@link FileDescriptor}.
 *
 * @apiNote
 * Like the other handlers and generators of this module, the {@code FromJsonGenerator} is meant to
 * operate on the common {@link JsOutput} passed on construction and thus its methods do not return
 * any generated code.
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
    static final String COMMENT =
            "The code for parsing the Protobuf messages of this file from the JSON data.";

    private final FileDescriptor file;
    private final JsOutput jsOutput;

    /**
     * Creates the new {@code FromJsonGenerator} which will process the given file descriptor.
     *
     * @param file
     *         the {@code FileDescriptor} whose messages to process
     * @param jsOutput
     *         the {@code JsOutput} to accumulate the generated JS code
     */
    FromJsonGenerator(FileDescriptor file, JsOutput jsOutput) {
        this.file = file;
        this.jsOutput = jsOutput;
    }

    /**
     * Generates the {@code fromJson(json)} method and all the related code for each message of the
     * processed {@code file}.
     *
     * <p>More specifically:
     * <ol>
     *     <li>Writes a comment explaining the generated code.
     *     <li>Adds an import for the
     *         {@linkplain io.spine.tools.protojs.knowntypes.KnownTypeParsersWriter standard type
     *         parsers}.
     *     <li>Adds the {@code fromJson(json)} method for each message which parses JSON
     *         {@code string} into object.
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
        jsOutput.addEmptyLine();
        jsOutput.addComment(COMMENT);
    }

    /**
     * Generates the {@link io.spine.tools.protojs.knowntypes.KnownTypeParsersWriter
     * known_type_parsers.js} import.
     *
     * <p>The import path is relative to the processed {@code file}.
     */
    @VisibleForTesting
    void generateParsersImport() {
        jsOutput.addEmptyLine();
        String fileName = file.getFullName();
        JsImportGenerator generator = JsImportGenerator.createFor(fileName);
        String parsersImport = generator.namedImport(KNOWN_TYPE_PARSERS, PARSERS_IMPORT_NAME);
        jsOutput.addLine(parsersImport);
    }

    /**
     * Generates the {@code fromJson(json)} and {@code fromObject(obj)} methods for each message of
     * the file.
     */
    @VisibleForTesting
    void generateMethods() {
        for (Descriptor message : file.getMessageTypes()) {
            MessageHandler handler = MessageHandler.createFor(message, jsOutput);
            handler.generateJs();
        }
    }
}
