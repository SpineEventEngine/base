/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.sample.map.types;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.spine.code.java.ClassName;
import io.spine.type.KnownTypes;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("KnownTypes should have an entry for")
class KnownTypesTest {

    private static final String PROTO_TYPE_PREFIX = "type.spine.io/spine.sample.map.types.";
    private static final String JAVA_PACKAGE_PREFIX = "io.spine.sample.map.types.";

    private static final String FIRST_MSG = "FirstMsg";
    private static final String SECOND_MSG = "SecondMsg";
    private static final String THIRD_MSG = "ThirdMsg";
    private static final String FOURTH_MSG = "FourthMsg";

    private static final String SECOND_ENUM = "SecondEnum";
    private static final String THIRD_ENUM = "ThirdEnum";
    private static final String FOURTH_ENUM = "FourthEnum";

    private static final String MSG_ONE = "Msg1";
    private static final String MSG_TWO = "Msg2";
    private static final String ENUM_TWO = "Enum2";

    private KnownTypes knownTypes;

    @BeforeEach
    void setUp() {
        knownTypes = KnownTypes.instance();
    }

    @Test
    @DisplayName("a simple message")
    void simpleMessage() {
        assertIsKnownType("SimpleMsg");
    }

    @Test
    @DisplayName("a simple enum")
    void simpleEnum() {
        assertIsKnownType("SimpleEnum");
    }

    @Test
    @DisplayName("a message with outer class which set in the protobuf file as an option")
    void messageWithOuterClassSetInFileOption() {
        assertIsKnownType("InnerMsg", "TestOuterClass$InnerMsg");
    }

    @Test
    @DisplayName("an enum with outer class which set in the protobuf file as an option")
    void enumWithOuterClassSetInFileOption() {
        assertIsKnownType("InnerEnum", "TestOuterClass$InnerEnum");
    }

    @Test
    @DisplayName("a message with outer class which passed as a file name")
    void messageWithOuterClassAsFileName() {
        assertIsKnownType("TestMsg", "OuterClassName$TestMsg");
    }

    @Test
    @DisplayName("an enum with outer class which passed as a file name")
    void enumWithOuterClassAsFileName() {
        assertIsKnownType("TestEnum", "OuterClassName$TestEnum");
    }

    @Test
    @DisplayName("the top level messages")
    void topLevelMessages() {
        assertIsKnownType(FIRST_MSG);
        assertIsKnownType(MSG_ONE);
    }

    @Test
    @DisplayName("the second level messages")
    void secondLevelMessages() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG));
        assertIsKnownType(compose(MSG_ONE, MSG_TWO));
    }

    @Test
    @DisplayName("a second level enum")
    void secondLevelEnum() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_ENUM));
        assertIsKnownType(compose(MSG_ONE, ENUM_TWO));
    }

    @Test
    @DisplayName("a third level message")
    void thirdLevelMessage() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_MSG));
    }

    @Test
    @DisplayName("a third level enum")
    void thirdLevelEnum() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_ENUM));
    }

    @Test
    @DisplayName("a fourth level message")
    void fourthLevelMessage() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_MSG, FOURTH_MSG));
    }

    @Test
    @DisplayName("a fourth level enum")
    void fourthLevelEnum() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_MSG, FOURTH_ENUM));
    }

    private void assertIsKnownType(String protoTypeName, String javaClassName) {
        TypeUrl url = TypeUrl.parse(PROTO_TYPE_PREFIX + protoTypeName);
        ClassName className = knownTypes.classNameOf(url);

        assertEquals(JAVA_PACKAGE_PREFIX + javaClassName, className.value());
    }

    private void assertIsKnownType(String typeName) {
        assertIsKnownType(typeName, typeName);
    }

    private void assertIsKnownType(Iterable<String> parentsAndTypeName) {
        String protoName = Joiner.on(".")
                                 .join(parentsAndTypeName);
        String javaName = Joiner.on("$")
                                .join(parentsAndTypeName);
        TypeUrl url = TypeUrl.parse(PROTO_TYPE_PREFIX + protoName);

        ClassName className = knownTypes.classNameOf(url);

        assertEquals(JAVA_PACKAGE_PREFIX + javaName, className.value());
    }

    private static Iterable<String> compose(String... elems) {
        return ImmutableList.copyOf(elems);
    }
}
