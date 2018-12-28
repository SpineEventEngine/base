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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.js.generate.output.CodeLines;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.code.js.LibraryFile.KNOWN_TYPE_PARSERS;
import static io.spine.code.js.LibraryFile.OBJECT_PARSER;
import static io.spine.js.generate.given.Generators.assertContains;
import static io.spine.js.generate.given.Given.file;
import static io.spine.js.generate.parse.FromJsonMethod.FROM_JSON;
import static io.spine.js.generate.parse.FromJsonMethod.FROM_OBJECT;
import static io.spine.js.generate.parse.ParseMethodsSnippet.COMMENT;

@DisplayName("ParseMethodsSnippet should")
class ParseMethodsSnippetTest {

    private final FileDescriptor file = file();
    private final ParseMethodsSnippet generator = new ParseMethodsSnippet(file);

    @Test
    @DisplayName("generate explaining comment")
    void generateComment() {
        CodeLines comment = generator.value();
        assertContains(comment, COMMENT.content());
    }

    @Test
    @DisplayName("generate imports")
    void generateImports() {
        CodeLines snippet = generator.imports();
        String knownTypeParsersImport = "require('../../" + KNOWN_TYPE_PARSERS + "');";
        String abstractParserImport = "require('../../" + OBJECT_PARSER + "');";
        assertContains(snippet, knownTypeParsersImport);
        assertContains(snippet, abstractParserImport);
    }

    @Test
    @DisplayName("generate `fromJson` and `fromObject` methods for all messages in file")
    void generateMethods() {
        CodeLines snippet = generator.parseMethods();
        for (Descriptor message : file.getMessageTypes()) {
            String fromJsonDeclaration = message.getFullName() + '.' + FROM_JSON;
            assertContains(snippet, fromJsonDeclaration);
            String fromObjectDeclaration = message.getFullName() + '.' + FROM_OBJECT;
            assertContains(snippet, fromObjectDeclaration);
        }
    }
}
