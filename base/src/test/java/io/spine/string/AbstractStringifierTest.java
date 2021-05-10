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

package io.spine.string;

import com.google.common.base.Converter;
import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertHasPrivateParameterlessCtor;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static java.lang.reflect.Modifier.isFinal;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The abstract base for stringifier tests.
 *
 * @param <T> the type of stringifier objects
 */
abstract class AbstractStringifierTest<T> {

    private final Stringifier<T> stringifier;
    private final Class<T> dataClass;

    AbstractStringifierTest(Stringifier<T> stringifier, Class<T> dataClass) {
        this.stringifier = stringifier;
        this.dataClass = dataClass;
    }

    protected abstract T createObject();

    static StringifierRegistry registry() {
        return StringifierRegistry.instance();
    }

    final Stringifier<T> stringifier() {
        return stringifier;
    }

    final Converter<String, T> parser() {
        return stringifier.reverse();
    }

    @Test
    @DisplayName("have private singleton constructor")
    void privateCtor() {
        assertHasPrivateParameterlessCtor(stringifier().getClass());
    }

    @Test
    @DisplayName("convert forward and backward")
    void convert() {
        T obj = createObject();

        final String str = stringifier.convert(obj);
        final T convertedBack = parser().convert(str);

        assertThat(convertedBack).isEqualTo(obj);
    }

    @Test
    @DisplayName("prohibit empty string input")
    void prohibitEmptyString() {
        assertIllegalArgument(() -> parser().convert(""));
    }

    @Test
    @DisplayName("serialize")
    void serialize() {
        Stringifier<T> expected = stringifier();
        Stringifier<T> stringifier = reserializeAndAssert(expected);
        assertThat(stringifier)
                .isSameInstanceAs(expected);
    }

    @Test
    @DisplayName("be registered")
    void isRegistered() {
        Optional<Stringifier<Object>> found = registry().find(dataClass);
        Truth8.assertThat(found).isPresent();
    }

    @Test
    @DisplayName("have final class")
    void isFinalClass() {
        Class<?> stringifierClass = stringifier.getClass();
        assertThat(isFinal(stringifierClass.getModifiers()))
                .isTrue();
    }
}
