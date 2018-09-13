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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.protojs.code.JsOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.protojs.given.Generators.assertContains;
import static io.spine.tools.protojs.given.Given.file;

/**
 * @author Dmytro Kuzmin
 */
@DisplayName("FromJsonGenerator should")
class FromJsonGeneratorTest {

    private FileDescriptor file;
    private JsOutput jsOutput;
    private FromJsonGenerator generator;

    @BeforeEach
    void setUp() {
        file = file();
        jsOutput = new JsOutput();
        generator = new FromJsonGenerator(file, jsOutput);
    }

    @Test
    @DisplayName("generate explaining comment")
    void generateComment() {
        generator.generateComment();
        assertGeneratedCodeContains(FromJsonGenerator.COMMENT);
    }

    @Test
    @DisplayName("generate known types and known type parsers imports")
    void generateImports() {
        generator.generateParsersImport();
        String knownTypeParsersImport = "require('../../known_type_parsers.js');";
        assertGeneratedCodeContains(knownTypeParsersImport);
    }

    @Test
    @DisplayName("generate `fromJson` and `fromObject` methods for all messages in file")
    void generateMethods() {
        generator.generateMethods();
        for (Descriptor message : file.getMessageTypes()) {
            String fromJsonDeclaration = message.getFullName() + ".fromJson";
            assertGeneratedCodeContains(fromJsonDeclaration);
            String fromObjectDeclaration = message.getFullName() + ".fromObject";
            assertGeneratedCodeContains(fromObjectDeclaration);
        }
    }

    private void assertGeneratedCodeContains(String toSearch) {
        assertContains(jsOutput, toSearch);
    }
}
