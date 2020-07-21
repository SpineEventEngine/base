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

package io.spine.tools.gradle.compiler;

import io.spine.query.EntityWithColumns;
import io.spine.tools.column.Project;
import io.spine.tools.column.ProjectName;
import io.spine.tools.column.ProjectViewWithColumns;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`ColumnGenPlugin` should")
class ColumnGenPluginTest {

    @Test
    @DisplayName("generate an interface, which extends `EntityWithColumns`")
    void generateInterface() {
        assertThat(ProjectViewWithColumns.class).isAssignableTo(EntityWithColumns.class);
    }

    @Test
    @DisplayName("generate a getter method in the interface for each entity column")
    void generateGetters() throws NoSuchMethodException {
        Class<ProjectViewWithColumns> generatedClass = ProjectViewWithColumns.class;
        Method[] declaredMethods = generatedClass.getDeclaredMethods();

        assertThat(declaredMethods).hasLength(2);

        Method getProjectName = generatedClass.getDeclaredMethod("getProjectName");
        assertThat(getProjectName.getReturnType()).isEqualTo(ProjectName.class);

        Method getStatus = generatedClass.getDeclaredMethod("getStatus");
        assertThat(getStatus.getReturnType()).isEqualTo(Project.Status.class);
    }
}
