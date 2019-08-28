/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.reflect;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.reflect.given.TypesTestEnv.ListOfMessages;
import io.spine.reflect.given.TypesTestEnv.TaskStatus;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.reflect.Types.argumentIn;
import static io.spine.reflect.Types.isEnumClass;
import static io.spine.reflect.Types.isMessageClass;
import static io.spine.reflect.Types.listTypeOf;
import static io.spine.reflect.Types.mapTypeOf;
import static io.spine.reflect.Types.resolveArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"SerializableNonStaticInnerClassWithoutSerialVersionUID",
        "SerializableInnerClassWithNonSerializableOuterClass"}) // OK when using TypeToken.
@DisplayName("Types utility class should")
class TypesTest extends UtilityClassTest<Types> {

    TypesTest() {
        super(Types.class);
    }

    @Test
    @DisplayName("create a map type")
    void createMapType() {
        Type type = mapTypeOf(String.class, Integer.class);
        Type expectedType = new TypeToken<Map<String, Integer>>(){}.getType();
        assertEquals(expectedType, type);
    }

    @Test
    @DisplayName("create a list type")
    void createListType() {
        Type type = listTypeOf(String.class);
        Type expectedType = new TypeToken<List<String>>(){}.getType();
        assertEquals(expectedType, type);
    }

    @Test
    @DisplayName("tell if the type is an enum class")
    void tellIfIsEnumClass() {

        assertThat(isEnumClass(TaskStatus.class))
                .isTrue();
        assertThat(isEnumClass(Message.class))
                .isFalse();
    }

    @Test
    @DisplayName("tell if the type is a message class")
    void tellIfIsMessageClass() {

        assertThat(isMessageClass(StringValue.class))
                .isTrue();
        assertThat(isMessageClass(TaskStatus.class))
                .isFalse();
    }

    @Test
    @DisplayName("resolve params of a generic type")
    void resolveTypeParams() {
        Type type = new TypeToken<Function<String, StringValue>>() {}.getType();
        ImmutableList<Type> types = resolveArguments(type);
        assertThat(types).containsExactly(String.class, StringValue.class);
    }

    @Test
    @DisplayName("return an empty list when resolving params of a non-parameterized type")
    void resolveRawTypeParams() {
        Type type = new TypeToken<String>() {}.getType();
        ImmutableList<Type> types = resolveArguments(type);
        assertThat(types).isEmpty();
    }

    @Test
    @DisplayName("obtain a type argument value from the inheritance chain")
    void getTypeArgument() {
        Class<?> argument = argumentIn(ListOfMessages.class, Iterable.class, 0);
        assertEquals(argument, Message.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.testStaticMethods(Types.class, NullPointerTester.Visibility.PACKAGE);
    }
}
