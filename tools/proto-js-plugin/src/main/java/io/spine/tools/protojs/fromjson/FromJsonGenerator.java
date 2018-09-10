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

public final class FromJsonGenerator {

    private static final String COMMENT = "The code generated for parsing the Protobuf messages " +
            "declared in this file from the JSON data.";
    public static final String PARSERS_IMPORT_NAME = "known_type_parsers";

    private final FileDescriptor file;
    private final JsGenerator jsGenerator;

    FromJsonGenerator(FileDescriptor file, JsGenerator jsGenerator) {
        this.file = file;
        this.jsGenerator = jsGenerator;
    }

    void generateJs() {
        generateComment();
        generateImports();
        generateMethods();
    }

    @VisibleForTesting
    void generateComment() {
        jsGenerator.addEmptyLine();
        jsGenerator.addComment(COMMENT);
    }

    @VisibleForTesting
    void generateImports() {
        jsGenerator.addEmptyLine();
        String fileName = file.getFullName();
        JsImportGenerator generator = JsImportGenerator.createFor(fileName);
        String parsersImport = generator.namedImport(KNOWN_TYPE_PARSERS, PARSERS_IMPORT_NAME);
        jsGenerator.addLine(parsersImport);
    }

    @VisibleForTesting
    void generateMethods() {
        for (Descriptor message : file.getMessageTypes()) {
            MessageHandler handler = MessageHandler.createFor(message, jsGenerator);
            handler.generateJs();
        }
    }
}
