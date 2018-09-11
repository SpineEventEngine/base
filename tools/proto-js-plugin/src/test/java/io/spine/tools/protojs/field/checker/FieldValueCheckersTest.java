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
import static io.spine.tools.protojs.field.checker.FieldValueCheckers.checkerFor;
import static io.spine.tools.protojs.given.Given.COMMANDS_PROTO;
import static io.spine.tools.protojs.given.Given.preparedProject;

@DisplayName("FieldValueCheckers utility should")
class FieldValueCheckersTest extends UtilityClassTest<FieldValueCheckers> {

    private FieldDescriptor messageField;
    private FieldDescriptor primitiveField;
    private FieldDescriptor timestampField;

    private JsGenerator jsGenerator;

    FieldValueCheckersTest() {
        super(FieldValueCheckers.class);
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
    @DisplayName("create checker for primitive field")
    void createForPrimitive() {
        FieldValueChecker checker = checkerFor(primitiveField, jsGenerator);
        assertInstanceOf(PrimitiveFieldChecker.class, checker);
    }

    @Test
    @DisplayName("create checker for message field")
    void createForMessage() {
        FieldValueChecker checker = checkerFor(messageField, jsGenerator);
        assertInstanceOf(MessageFieldChecker.class, checker);
    }

    @Test
    @DisplayName("create message checker for standard type field")
    void createForWellKnown() {
        FieldValueChecker checker = checkerFor(timestampField, jsGenerator);
        assertInstanceOf(MessageFieldChecker.class, checker);
    }
}
