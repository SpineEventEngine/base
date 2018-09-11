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

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.given.Given.PreparedProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.testing.Verify.assertInstanceOf;
import static io.spine.tools.protojs.field.parser.FieldValueParsers.parserFor;
import static io.spine.tools.protojs.given.Given.COMMANDS_PROTO;
import static io.spine.tools.protojs.given.Given.preparedProject;

@DisplayName("FieldValueParsers utility should")
class FieldValueParsersTest extends UtilityClassTest<FieldValueParsers> {

    private FieldDescriptor messageField;
    private FieldDescriptor primitiveField;
    private FieldDescriptor timestampField;

    private JsGenerator jsGenerator;

    FieldValueParsersTest() {
        super(FieldValueParsers.class);
    }

    @Override
    protected void setDefaults(NullPointerTester tester) {
        tester.setDefault(FieldDescriptor.class, messageField);
    }

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
    @DisplayName("create parser for message field with custom type")
    void createParserForMessage() {
        FieldValueParser parser = parserFor(messageField, jsGenerator);
        assertInstanceOf(MessageFieldParser.class, parser);
    }

    @Test
    @DisplayName("create parser for primitive field")
    void createParserForPrimitive() {
        FieldValueParser parser = parserFor(primitiveField, jsGenerator);
        assertInstanceOf(PrimitiveFieldParser.class, parser);
    }

    @Test
    @DisplayName("create parser for message field with standard type")
    void createParserForWellKnown() {
        FieldValueParser parser = parserFor(timestampField, jsGenerator);
        assertInstanceOf(WellKnownFieldParser.class, parser);
    }
}
