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

package io.spine.tools.protojs.types;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.protojs.given.Given.PreparedProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.tools.protojs.given.Given.COMMANDS_PROTO;
import static io.spine.tools.protojs.given.Given.preparedProject;
import static io.spine.tools.protojs.types.Types.PREFIX;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Types utility should")
class TypesTest extends UtilityClassTest<Types> {

    private FileDescriptor file;

    TypesTest() {
        super(Types.class);
    }

    @BeforeEach
    void setUp() {
        PreparedProject project = preparedProject();
        FileSet fileSet = project.fileSet();
        FileName fileName = FileName.of(COMMANDS_PROTO);
        Optional<FileDescriptor> fileDescriptor = fileSet.tryFind(fileName);
        file = fileDescriptor.get();
    }

    @Test
    @DisplayName("return type with `proto.` prefix for message type")
    void addPrefixForMessage() {
        Descriptor message = file.getMessageTypes()
                                 .get(0);
        String typeWithProtoPrefix = typeWithProtoPrefix(message);
        String expected = PREFIX + message.getFullName();
        assertEquals(expected, typeWithProtoPrefix);
    }

    @Test
    @DisplayName("return type with `proto.` prefix for enum type")
    void addPrefixForEnum() {
        EnumDescriptor enumType = file.getEnumTypes()
                                      .get(0);
        String typeWithProtoPrefix = typeWithProtoPrefix(enumType);
        String expected = PREFIX + enumType.getFullName();
        assertEquals(expected, typeWithProtoPrefix);
    }
}
