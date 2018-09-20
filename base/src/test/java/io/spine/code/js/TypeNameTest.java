///*
// * Copyright 2018, TeamDev. All rights reserved.
// *
// * Redistribution and use in source and/or binary forms, with or without
// * modification, must retain the above copyright notice and the following
// * disclaimer.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//
//package io.spine.code.js;
//
//import com.google.protobuf.Descriptors.Descriptor;
//import com.google.protobuf.Descriptors.EnumDescriptor;
//import com.google.protobuf.Descriptors.FileDescriptor;
//import com.google.protobuf.Timestamp;
//import io.spine.option.OptionsProto;
//import io.spine.testing.UtilityClassTest;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import spine.test.protojs.Task.TaskId;
//
//import static io.spine.tools.protojs.given.Given.enumType;
//import static io.spine.tools.protojs.given.Given.message;
//import static io.spine.tools.protojs.types.Types.PREFIX;
//import static io.spine.tools.protojs.types.Types.isStandardOrSpineOptions;
//import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
///**
// * @author Dmytro Kuzmin
// */
//@DisplayName("Types utility should")
//class TypeNameTest extends UtilityClassTest<Types> {
//
//    TypeNameTest() {
//        super(Types.class);
//    }
//
//    @Test
//    @DisplayName("return type with `proto.` prefix for message type")
//    void addPrefixForMessage() {
//        Descriptor message = Given.message();
//        String typeWithProtoPrefix = typeWithProtoPrefix(message);
//        String expected = PREFIX + message.getFullName();
//        assertEquals(expected, typeWithProtoPrefix);
//    }
//
//    @Test
//    @DisplayName("return type with `proto.` prefix for enum type")
//    void addPrefixForEnum() {
//        EnumDescriptor enumType = Given.enumType();
//        String typeWithProtoPrefix = typeWithProtoPrefix(enumType);
//        String expected = PREFIX + enumType.getFullName();
//        assertEquals(expected, typeWithProtoPrefix);
//    }
//
//    @Test
//    @DisplayName("check if file is standard type")
//    void checkStandardType() {
//        FileDescriptor timestamp = Timestamp.getDescriptor()
//                                            .getFile();
//        assertTrue(isStandardOrSpineOptions(timestamp));
//
//        FileDescriptor taskId = TaskId.getDescriptor()
//                                      .getFile();
//        assertFalse(isStandardOrSpineOptions(taskId));
//    }
//
//    @Test
//    @DisplayName("check if file is Spine Options")
//    void checkSpineOptions() {
//        FileDescriptor options = OptionsProto.getDescriptor()
//                                               .getFile();
//        assertTrue(isStandardOrSpineOptions(options));
//    }
//}
