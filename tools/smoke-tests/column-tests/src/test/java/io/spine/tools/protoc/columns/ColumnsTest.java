/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.columns;

import com.google.common.truth.Correspondence;
import io.spine.base.EntityColumn;
import io.spine.tools.column.ProjectView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;

@SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
@DisplayName("`ProtocPlugin`, when generating entity columns, should")
class ColumnsTest {

    @Test
    @DisplayName("generate a nested `Column` class with a private c-tor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(ProjectView.Column.class);
    }

    @Test
    @DisplayName("generate a method which returns an `EntityColumn` for each message column")
    void generateColumnMethods() {
        checkColumnName(ProjectView.Column.projectName(), "project_name");
        checkColumnName(ProjectView.Column.status(), "status");
    }

    @Test
    @DisplayName("ignore nested columns")
    void ignoreNestedColumns() {
        assertDoesNotContainMethod(ProjectView.Column.class, "assignee");
        assertDoesNotContainMethod(ProjectView.Column.class, "name");
    }

    private static void checkColumnName(EntityColumn column, String expectedName) {
        assertThat(column.name().value()).isEqualTo(expectedName);
    }

    private static void assertDoesNotContainMethod(Class<?> type, String methodNames) {
        Method[] methods = type.getDeclaredMethods();
        assertThat(methods).asList()
                           .comparingElementsUsing(nameCorrespondence())
                           .doesNotContain(methodNames);
    }

    private static Correspondence<Method, String> nameCorrespondence() {
        return Correspondence.from(ColumnsTest::hasName, "has name");
    }

    private static boolean hasName(Method method, String name) {
        return name.equals(method.getName());
    }
}
