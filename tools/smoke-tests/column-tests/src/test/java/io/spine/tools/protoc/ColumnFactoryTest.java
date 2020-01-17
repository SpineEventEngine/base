/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc;

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
class ColumnFactoryTest {

    @Test
    @DisplayName("generate a nested `Columns` class with a private c-tor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(ProjectView.Columns.class);
    }

    @Test
    @DisplayName("generate a method which returns an `EntityColumn` for each message column")
    void generateColumnMethods() {
        checkNameAndType(ProjectView.Columns.projectName(), "project_name");
        checkNameAndType(ProjectView.Columns.status(), "status");
    }

    @Test
    @DisplayName("ignore nested columns")
    void ignoreNestedColumns() {
        assertDoesNotContainMethod(ProjectView.Columns.class, "assignee");
        assertDoesNotContainMethod(ProjectView.Columns.class, "name");
    }

    private static void checkNameAndType(EntityColumn<ProjectView> column, String name) {
        assertThat(column.name()).isEqualTo(name);
        assertThat(column.messageType()).isEqualTo(ProjectView.class);
    }

    private static void assertDoesNotContainMethod(Class<?> type, String methodNames) {
        Method[] methods = type.getDeclaredMethods();
        assertThat(methods).asList()
                           .comparingElementsUsing(nameCorrespondence())
                           .doesNotContain(methodNames);
    }

    private static Correspondence<Method, String> nameCorrespondence() {
        return Correspondence.from(ColumnFactoryTest::hasName, "has the same name");
    }

    private static boolean hasName(Method method, String name) {
        return name.equals(method.getName());
    }
}
