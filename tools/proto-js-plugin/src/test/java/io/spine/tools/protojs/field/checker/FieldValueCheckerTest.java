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

package io.spine.tools.protojs.field.checker;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.given.Given;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.tools.protojs.field.checker.FieldValueCheckers.checkerFor;
import static io.spine.tools.protojs.given.Generators.assertGeneratedCodeContains;
import static io.spine.tools.protojs.given.Given.COMMANDS_PROTO;
import static io.spine.tools.protojs.given.Given.preparedProject;
import static java.lang.String.format;

@DisplayName("FieldValueChecker should")
class FieldValueCheckerTest {

    private static final String FIELD_VALUE = "value";
    private static final String SETTER_FORMAT = "set(%s)";
    private FieldDescriptor messageField;
    private FieldDescriptor primitiveField;

    private JsGenerator jsGenerator;

    @BeforeEach
    void setUp() {
        Given.PreparedProject project = preparedProject();
        FileSet fileSet = project.fileSet();
        FileName fileName = FileName.of(COMMANDS_PROTO);
        Optional<Descriptors.FileDescriptor> fileDescriptor = fileSet.tryFind(fileName);
        Descriptors.FileDescriptor commandsProto = fileDescriptor.get();
        Descriptors.Descriptor createTask = commandsProto.getMessageTypes()
                                                         .get(0);
        messageField = createTask.getFields()
                                 .get(0);
        primitiveField = createTask.getFields()
                                   .get(1);
        jsGenerator = new JsGenerator();
    }

    @Test
    @DisplayName("generate code to enter non-null check for primitive")
    void enterPrimitiveCheck() {
        FieldValueChecker checker = checkerFor(primitiveField, jsGenerator);
        checker.performNullCheck(FIELD_VALUE, SETTER_FORMAT);
        String check = "if (" + FIELD_VALUE + " !== null)";
        assertGeneratedCodeContains(jsGenerator, check);
    }

    @Test
    @DisplayName("generate code to enter null check for message")
    void enterMessageCheck() {
        FieldValueChecker checker = checkerFor(messageField, jsGenerator);
        checker.performNullCheck(FIELD_VALUE, SETTER_FORMAT);
        String check = "if (" + FIELD_VALUE + " === null)";
        assertGeneratedCodeContains(jsGenerator, check);
    }

    @Test
    @DisplayName("set field value to null in case of message")
    void setMessageToNull() {
        FieldValueChecker checker = checkerFor(messageField, jsGenerator);
        checker.performNullCheck(FIELD_VALUE, SETTER_FORMAT);
        String setNull = format(SETTER_FORMAT, "null");
        assertGeneratedCodeContains(jsGenerator, setNull);
    }
}
