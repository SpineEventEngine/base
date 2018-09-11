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

package io.spine.tools.protojs.field;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.protojs.given.Given.PreparedProject;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;
import static io.spine.tools.protojs.field.Fields.capitalizedName;
import static io.spine.tools.protojs.field.Fields.isMap;
import static io.spine.tools.protojs.field.Fields.isMessage;
import static io.spine.tools.protojs.field.Fields.isRepeated;
import static io.spine.tools.protojs.field.Fields.isWellKnownType;
import static io.spine.tools.protojs.field.Fields.keyDescriptor;
import static io.spine.tools.protojs.field.Fields.valueDescriptor;
import static io.spine.tools.protojs.given.Given.COMMANDS_PROTO;
import static io.spine.tools.protojs.given.Given.preparedProject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Fields utility should")
class FieldsTest extends UtilityClassTest<Fields> {

    private FieldDescriptor messageField;
    private FieldDescriptor primitiveField;
    private FieldDescriptor timestampField;
    private FieldDescriptor repeatedField;
    private FieldDescriptor mapField;

    FieldsTest() {
        super(Fields.class);
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
        repeatedField = createTask.getFields()
                                  .get(3);
        mapField = createTask.getFields()
                             .get(4);
    }

    @Test
    @DisplayName("tell if field is message")
    void tellIfMessage() {
        assertTrue(isMessage(messageField));
        assertFalse(isMessage(primitiveField));
        assertTrue(isMessage(repeatedField));
    }

    @Test
    @DisplayName("tell if field is standard type with known parser")
    void tellIfWellKnownType() {
        assertTrue(isWellKnownType(timestampField));
        assertFalse(isWellKnownType(messageField));
        assertFalse(isWellKnownType(primitiveField));
    }

    @Test
    @DisplayName("tell if field is repeated")
    void tellIfRepeated() {
        assertTrue(isRepeated(repeatedField));
    }

    @Test
    @DisplayName("tell if field is map")
    void tellIfMap() {
        assertTrue(isMap(mapField));
    }

    @Test
    @DisplayName("not mark map field as repeated")
    void notMarkMapAsRepeated() {
        assertFalse(isRepeated(mapField));
    }

    @Test
    @DisplayName("get key descriptor for map field")
    void getKeyDescriptor() {
        FieldDescriptor key = keyDescriptor(mapField);
        assertEquals(INT64, key.getType());
    }

    @Test
    @DisplayName("throw ISE if getting key descriptor from non-map field")
    void getKeyOnlyFromMap() {
        assertThrows(IllegalStateException.class, () -> keyDescriptor(repeatedField));
    }

    @Test
    @DisplayName("get value descriptor for map field")
    void getValueDescriptor() {
        FieldDescriptor value = valueDescriptor(mapField);
        assertEquals(MESSAGE, value.getType());
        Descriptor messageType = value.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(messageType);
        String expected = "type.spine.io/spine.sample.protojs.TaskId";
        assertEquals(expected, typeUrl.toString());
    }

    @Test
    @DisplayName("throw ISE if getting value descriptor from non-map field")
    void getValueOnlyFromMap() {
        assertThrows(IllegalStateException.class, () -> valueDescriptor(repeatedField));
    }

    @Test
    @DisplayName("return capitalized field name")
    void getCapitalizedName() {
        String capitalizedName = capitalizedName(messageField);
        String expected = "MessageField";
        assertEquals(expected, capitalizedName);
    }
}
