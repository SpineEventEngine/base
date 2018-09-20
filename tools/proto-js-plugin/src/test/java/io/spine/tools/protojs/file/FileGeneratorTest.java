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

package io.spine.tools.protojs.file;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.generate.JsOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.code.js.CommonFileName.KNOWN_TYPE_PARSERS;
import static io.spine.generate.given.Generators.assertContains;
import static io.spine.tools.protojs.file.FileGenerator.COMMENT;
import static io.spine.tools.protojs.given.Given.file;
import static io.spine.tools.protojs.message.MessageGenerator.FROM_JSON;
import static io.spine.tools.protojs.message.MessageGenerator.FROM_OBJECT;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("FileGenerator should")
class FileGeneratorTest {

    private FileDescriptor file;
    private JsOutput jsOutput;
    private FileGenerator generator;

    @BeforeEach
    void setUp() {
        file = file();
        jsOutput = new JsOutput();
        generator = new FileGenerator(file, jsOutput);
    }

    @Test
    @DisplayName("generate explaining comment")
    void generateComment() {
        generator.generateComment();
        assertContains(jsOutput, COMMENT);
    }

    @Test
    @DisplayName("generate known type parsers imports")
    void generateImports() {
        generator.generateParsersImport();
        String knownTypeParsersImport = "require('../../" + KNOWN_TYPE_PARSERS + "');";
        assertContains(jsOutput, knownTypeParsersImport);
    }

    @Test
    @DisplayName("generate `fromJson` and `fromObject` methods for all messages in file")
    void generateMethods() {
        generator.generateMethods();
        for (Descriptor message : file.getMessageTypes()) {
            String fromJsonDeclaration = message.getFullName() + '.' + FROM_JSON;
            assertContains(jsOutput, fromJsonDeclaration);
            String fromObjectDeclaration = message.getFullName() + '.' + FROM_OBJECT;
            assertContains(jsOutput, fromObjectDeclaration);
        }
    }
}
