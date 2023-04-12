/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.reflect;

import com.google.common.reflect.TypeToken;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.Truth;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.reflect.given.TypesTestEnv.ListOfMessages;
import io.spine.reflect.given.TypesTestEnv.TaskStatus;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"SerializableNonStaticInnerClassWithoutSerialVersionUID",
        "SerializableInnerClassWithNonSerializableOuterClass"}) // OK when using TypeToken.
@DisplayName("`Types` utility class should")
class TypesTest extends UtilityClassTest<Types> {

    TypesTest() {
        super(Types.class);
    }

    @Test
    @DisplayName("create a map type")
    void createMapType() {
        var type = Types.mapTypeOf(String.class, Integer.class);
        var expectedType = new TypeToken<Map<String, Integer>>(){}.getType();
        assertEquals(expectedType, type);
    }

    @Test
    @DisplayName("create a list type")
    void createListType() {
        var type = Types.listTypeOf(String.class);
        var expectedType = new TypeToken<List<String>>(){}.getType();
        assertEquals(expectedType, type);
    }

    @Test
    @DisplayName("tell if the type is an enum class")
    void tellIfIsEnumClass() {
        Truth.assertThat(Types.isEnumClass(TaskStatus.class))
             .isTrue();
        Truth.assertThat(Types.isEnumClass(Message.class))
             .isFalse();
    }

    @Test
    @DisplayName("resolve params of a generic type")
    void resolveTypeParams() {
        var type = new TypeToken<Function<String, StringValue>>() {}.getType();
        var types = Types.resolveArguments(type);
        Truth.assertThat(types).containsExactly(String.class, StringValue.class);
    }

    @Test
    @DisplayName("return an empty list when resolving params of a non-parameterized type")
    void resolveRawTypeParams() {
        var type = new TypeToken<String>() {}.getType();
        var types = Types.resolveArguments(type);
        Truth.assertThat(types).isEmpty();
    }

    @Nested
    @DisplayName("obtain a generic type argument")
    class TypeArguments {

        @Test
        @DisplayName("from the inheritance chain")
        void getTypeArgument() {
            var argument = Types.argumentIn(ListOfMessages.class, Iterable.class, 0);
            assertEquals(argument, Message.class);
        }

        @Test
        @DisplayName("assuming generic superclass")
        void assumingGenericSuperclass() {
            var val = new Parametrized<Long, String>() {};
            Assertions.assertEquals(Long.class, Types.argumentIn(val.getClass(), Base.class, 0));
            Assertions.assertEquals(String.class, Types.argumentIn(val.getClass(), Base.class, 1));
        }

        @Test
        @DisplayName("obtain generic argument via superclass")
        void viaSuperclass() {
            Assertions.assertEquals(String.class, Types.argumentIn(Leaf.class, Base.class, 0));
            Assertions.assertEquals(Float.class, Types.argumentIn(Leaf.class, Base.class, 1));
        }
    }


    @SuppressWarnings({"EmptyClass", "unused"})
    private static class Base<T, K> {}

    private static class Parametrized<T, K> extends Base<T, K> {}

    private static class Leaf extends Base<String, Float> {}

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.testStaticMethods(Types.class, NullPointerTester.Visibility.PACKAGE);
    }
}
