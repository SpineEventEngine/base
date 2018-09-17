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

package io.spine.tools.protojs.code;

import com.google.common.testing.NullPointerTester;
import io.spine.tools.protojs.generate.JsImportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
// Generated code duplication needed to check main class.
@DisplayName("JsImportGenerator should")
class JsImportGeneratorTest {

    private static final String FILE_PATH = "root1/test/test.js";
    private static final String FILE_TO_IMPORT = "root2/tools/test_2.js";
    private static final String IMPORT_NAME = "test_2";

    private JsImportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = JsImportGenerator.createFor(FILE_PATH);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicInstanceMethods(generator);
    }

    @Test
    @DisplayName("generate import relative to file location")
    void generateImport() {
        String statement = generator.importStatement(FILE_TO_IMPORT);
        String pathToImport = "../../" + FILE_TO_IMPORT;
        String expected = "require('" + pathToImport + "');";
        assertEquals(expected, statement);
    }

    @Test
    @DisplayName("generate named import")
    void generateNamedImport() {
        String statement = generator.namedImport(FILE_TO_IMPORT, IMPORT_NAME);
        String pathToImport = "../../" + FILE_TO_IMPORT;
        String expected = "let " + IMPORT_NAME + " = require('" + pathToImport + "');";
        assertEquals(expected, statement);
    }

    @Test
    @DisplayName("generate raw import statement")
    void generateRawImport() {
        String statement = JsImportGenerator.rawImport(FILE_TO_IMPORT);
        String expected = "require('" + FILE_TO_IMPORT + "');";
        assertEquals(expected, statement);
    }

    @Test
    @DisplayName("generate raw named import statement")
    void generateRawNamedImport() {
        String statement = JsImportGenerator.rawNamedImport(FILE_TO_IMPORT, IMPORT_NAME);
        String expected = "let " + IMPORT_NAME + " = require('" + FILE_TO_IMPORT + "');";
        assertEquals(expected, statement);
    }
}
