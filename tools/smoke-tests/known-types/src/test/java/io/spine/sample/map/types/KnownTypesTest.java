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

package io.spine.sample.map.types;

import com.google.common.base.Joiner;
import io.spine.type.ClassName;
import io.spine.type.KnownTypes;
import io.spine.type.TypeUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.copyOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Litus
 */
@DisplayName("KnownTypes should")
public class KnownTypesTest {

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
    public void setUp() {
        knownTypes = KnownTypes.instance();
    }

    @Test
    @DisplayName("put an entry for the simple message")
    public void put_entry_for_simple_message() {
        assertIsKnownType("SimpleMsg");
    }

    @Test
    @DisplayName("put an entry for the simple enum")
    public void put_entry_for_simple_enum() {
        assertIsKnownType("SimpleEnum");
    }

    @Test
    @DisplayName("put an entry for the message with outer class which set in the protobuf file as an option")
    public void put_entry_for_msg_with_outer_class_set_in_protobuf_file_option() {
        assertIsKnownType("InnerMsg", "TestOuterClass$InnerMsg");
    }

    @Test
    @DisplayName("put an entry for the enum with outer class which set in the protobuf file as an option")
    public void put_entry_for_enum_with_outer_class_set_in_protobuf_file_option() {
        assertIsKnownType("InnerEnum", "TestOuterClass$InnerEnum");
    }

    @Test
    @DisplayName("put an entry for the message with outer class which passed as a file name")
    public void put_entry_for_msg_with_outer_class_as_file_name() {
        assertIsKnownType("TestMsg", "OuterClassName$TestMsg");
    }

    @Test
    @DisplayName("put an entry for the enum with outer class which passed as a file name")
    public void put_entry_for_enum_with_outer_class_as_file_name() {
        assertIsKnownType("TestEnum", "OuterClassName$TestEnum");
    }

    @Test
    @DisplayName("put an entry for the top level messages")
    public void put_entry_for_top_level_messages() {
        assertIsKnownType(FIRST_MSG);
        assertIsKnownType(MSG_ONE);
    }

    @Test
    @DisplayName("put an entry for the second level messages")
    public void put_entry_for_second_level_messages() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG));
        assertIsKnownType(compose(MSG_ONE, MSG_TWO));
    }

    @Test
    @DisplayName("put an entry for the second level enum")
    public void put_entry_for_second_level_enum() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_ENUM));
        assertIsKnownType(compose(MSG_ONE, ENUM_TWO));
    }

    @Test
    @DisplayName("put an entry for the third level message")
    public void put_entry_for_third_level_msg() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_MSG));
    }

    @Test
    @DisplayName("put an entry for the third level enum")
    public void put_entry_for_third_level_enum() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_ENUM));
    }

    @Test
    @DisplayName("put an entry for the fourth level message")
    public void put_entry_for_fourth_level_msg() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_MSG, FOURTH_MSG));
    }

    @Test
    @DisplayName("put an entry for the fourth level enum")
    public void put_entry_for_fourth_level_enum() {
        assertIsKnownType(compose(FIRST_MSG, SECOND_MSG, THIRD_MSG, FOURTH_ENUM));
    }

    private void assertIsKnownType(String protoTypeName, String javaClassName) {
        TypeUrl url = TypeUrl.parse(PROTO_TYPE_PREFIX + protoTypeName);
        ClassName className = knownTypes.getClassName(url);

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

        ClassName className = knownTypes.getClassName(url);

        assertEquals(JAVA_PACKAGE_PREFIX + javaName, className.value());
    }

    private static Iterable<String> compose(String... elems) {
        return copyOf(elems);
    }
}
