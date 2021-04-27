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

package io.spine.tools.protoc.given;

import com.google.common.truth.Correspondence;
import io.spine.query.EntityColumn;

import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;

/**
 * A test environment and utilities for {@link io.spine.tools.java.protoc.ColumnsTest}.
 */
public final class ColumnsTestEnv {

    /**
     * Prevents this test environment utility from an instantiation.
     */
    private ColumnsTestEnv() {
    }

    public static void checkColumnName(EntityColumn<?, ?> column, String expectedName) {
        assertThat(column.name().value()).isEqualTo(expectedName);
    }

    public static void assertDoesNotContainMethod(Class<?> type, String methodNames) {
        Method[] methods = type.getDeclaredMethods();
        assertThat(methods).asList()
                           .comparingElementsUsing(nameCorrespondence())
                           .doesNotContain(methodNames);
    }

    private static Correspondence<Method, String> nameCorrespondence() {
        return Correspondence.from(ColumnsTestEnv::hasName, "has name");
    }

    private static boolean hasName(Method method, String name) {
        return name.equals(method.getName());
    }
}
