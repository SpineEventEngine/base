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

package io.spine.js.generate.snippet;

import com.google.common.testing.NullPointerTester;
import io.spine.code.js.FileName;
import io.spine.js.generate.CodeLines;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static io.spine.code.js.FileName.of;
import static io.spine.js.generate.given.Generators.assertContains;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("JsImportGenerator should")
class JsImportGeneratorTest {

    private static final FileName FILE_PATH = of("root1/test/commands_pb.js");
    private static final FileName FILE_TO_IMPORT = of("root2/tools/tasks_pb.js");
    private static final Collection<FileName> IMPORTS = singletonList(FILE_TO_IMPORT);
    private static final String IMPORT_NAME = "import_name";
    private static final String LIB_TO_IMPORT = "base64-lib";

    private CodeLines jsOutput;
    private JsImportGenerator generator;

    @BeforeEach
    void setUp() {
        jsOutput = new CodeLines();
        generator = JsImportGenerator
                .newBuilder()
                .setFileName(FILE_PATH)
                .setImports(IMPORTS)
                .setJsOutput(jsOutput)
                .build();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().setDefault(FileName.class, FILE_TO_IMPORT)
                               .testAllPublicInstanceMethods(generator);
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // Method called to throw exception.
    @Test
    @DisplayName("not allow being created without JsOutput")
    void requireJsOutput() {
        JsImportGenerator.Builder builder = JsImportGenerator
                .newBuilder()
                .setFileName(FILE_PATH)
                .setImports(IMPORTS);
        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    @DisplayName("generate imports specified on creation")
    void generateImport() {
        generator.generate();
        String pathToImport = "../../" + FILE_TO_IMPORT;
        String expected = "require('" + pathToImport + "');";
        assertContains(jsOutput, expected);
    }

    @Test
    @DisplayName("generate non-relative imports if own file path was not specified")
    void generateNonRelative() {
        JsImportGenerator generator = JsImportGenerator
                .newBuilder()
                .setImports(IMPORTS)
                .setJsOutput(jsOutput)
                .build();
        generator.generate();
        String expected = "require('./" + FILE_TO_IMPORT + "');";
        assertContains(jsOutput, expected);
    }

    @Test
    @DisplayName("generate named import for file")
    void generateNamedImport() {
        generator.importFile(FILE_TO_IMPORT, IMPORT_NAME);
        String pathToImport = "../../" + FILE_TO_IMPORT;
        String expected = "let " + IMPORT_NAME + " = require('" + pathToImport + "');";
        assertContains(jsOutput, expected);
    }

    @Test
    @DisplayName("generate named import for lib")
    void generateRawImport() {
        generator.importLib(LIB_TO_IMPORT, IMPORT_NAME);
        String expected = "let " + IMPORT_NAME + " = require('" + LIB_TO_IMPORT + "');";
        assertContains(jsOutput, expected);
    }
}
