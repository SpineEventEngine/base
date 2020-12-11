/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.type;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DescriptorProtos.FileOptions;
import io.spine.option.OptionsProto;
import io.spine.testing.UtilityClassTest;
import io.spine.type.KnownTypes;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.TestValues.randomString;
import static io.spine.tools.type.MoreKnownTypes.extendWith;
import static java.nio.file.Files.newOutputStream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`MoreKnownTypes` should")
class MoreKnownTypesTest extends UtilityClassTest<MoreKnownTypes> {

    private File descriptorFile;

    MoreKnownTypesTest() {
        super(MoreKnownTypes.class);
    }

    @BeforeEach
    void setUp(@TempDir Path tempdir) throws IOException {
        descriptorFile = tempdir.resolve("more_known_types.desc").toFile();
        descriptorFile.createNewFile();
        FieldDescriptorProto field = FieldDescriptorProto
                .newBuilder()
                .setType(TYPE_STRING)
                .setName("string_value")
                .setNumber(1)
                .build();
        DescriptorProto newMessageType = DescriptorProto
                .newBuilder()
                .setName("TestDynamicType")
                .addField(field)
                .build();
        FileOptions options = FileOptions
                .newBuilder()
                .setExtension(OptionsProto.typeUrlPrefix, TypeUrl.Prefix.SPINE.value())
                .setJavaMultipleFiles(true)
                .build();
        FileDescriptorProto newProtoFile = FileDescriptorProto
                .newBuilder()
                .setName("test/test_dynamic_file.proto")
                .setPackage("spine.test")
                .addMessageType(newMessageType)
                .setOptions(options)
                .addDependency("spine/options.proto")
                .build();
        FileDescriptorSet set = FileDescriptorSet
                .newBuilder()
                .addFile(newProtoFile)
                .build();
        try (OutputStream stream = newOutputStream(descriptorFile.toPath())) {
            set.writeTo(stream);
        }
    }

    @Test
    @DisplayName("not allow non-existing files")
    void notAllowRandomFiles() {
        File nonExistingFile = new File(randomString());
        assertIllegalArgument(() -> extendWith(nonExistingFile));
    }

    @Test
    @DisplayName("extend known type set")
    void extendKnownTypes() {
        TypeUrl dynamicType = TypeUrl.parse("type.spine.io/spine.test.TestDynamicType");
        assertFalse(KnownTypes.instance().contains(dynamicType));
        MoreKnownTypes.extendWith(descriptorFile);
        assertTrue(KnownTypes.instance().contains(dynamicType));
    }
}
