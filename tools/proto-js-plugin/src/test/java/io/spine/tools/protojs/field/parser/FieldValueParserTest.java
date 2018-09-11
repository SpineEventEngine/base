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

package io.spine.tools.protojs.field.parser;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.given.Given.PreparedProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.tools.protojs.given.Generators.assertGeneratedCodeContains;
import static io.spine.tools.protojs.given.Given.COMMANDS_PROTO;
import static io.spine.tools.protojs.given.Given.preparedProject;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

@DisplayName("FieldValueParser should")
class FieldValueParserTest {

    private static final String VALUE = "value";
    private static final String VARIABLE = "variable";

    private FieldDescriptor messageField;
    private FieldDescriptor primitiveField;
    private FieldDescriptor timestampField;
    private JsGenerator jsGenerator;

    @BeforeEach
    void setUp() {
        PreparedProject project = preparedProject();
        FileSet fileSet = project.fileSet();
        FileName fileName = FileName.of(COMMANDS_PROTO);
        Optional<FileDescriptor> fileDescriptor = fileSet.tryFind(fileName);
        FileDescriptor commandsProto = fileDescriptor.get();
        Descriptor createTask = commandsProto.getMessageTypes()
                                             .get(0);
        messageField = createTask.getFields()
                                 .get(0);
        primitiveField = createTask.getFields()
                                   .get(1);
        timestampField = createTask.getFields()
                                   .get(2);
        jsGenerator = new JsGenerator();
    }

    @Test
    @DisplayName("parse message field with custom type via recursive call to fromObject")
    void parseMessage() {
        FieldValueParser parser = FieldValueParsers.parserFor(messageField, jsGenerator);
        parser.parseIntoVariable(VALUE, VARIABLE);
        Descriptor messageType = messageField.getMessageType();
        String type = typeWithProtoPrefix(messageType);
        String parse = "let " + VARIABLE + " = " + type + ".fromObject(" + VALUE + ')';
        assertGeneratedCodeContains(jsGenerator, parse);
    }

    @Test
    @DisplayName("parse primitive field via predefined code")
    void parsePrimitive() {
        FieldValueParser parser = FieldValueParsers.parserFor(primitiveField, jsGenerator);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parseInt(" + VALUE + ')';
        assertGeneratedCodeContains(jsGenerator, parse);
    }

    @Test
    @DisplayName("parse message field with standard type via known type parser")
    void parseWellKnown() {
        FieldValueParser parser = FieldValueParsers.parserFor(timestampField, jsGenerator);
        parser.parseIntoVariable(VALUE, VARIABLE);
        String parse = "let " + VARIABLE + " = parser.parse(" + VALUE + ')';
        assertGeneratedCodeContains(jsGenerator, parse);
    }
}
