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

package io.spine.tools.protoc;

import io.spine.tools.column.Project;
import io.spine.tools.column.ProjectCreation;
import io.spine.tools.column.ProjectView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertHasPrivateParameterlessCtor;
import static io.spine.tools.protoc.given.ColumnsTestEnv.assertDoesNotContainMethod;
import static io.spine.tools.protoc.given.ColumnsTestEnv.checkColumnName;

@SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
@DisplayName("`ProtocPlugin`, when generating entity columns, should")
class ColumnsTest {

    @Nested
    @DisplayName("handle messages declared as `AGGREGATE` state and")
    class Aggregate {

        @Test
        @DisplayName("generate a nested `Column` class with a private c-tor")
        void havePrivateCtor() {
            assertHasPrivateParameterlessCtor(Project.Column.class);
        }

        @Test
        @DisplayName("generate a method which returns an `EntityColumn` for each message column")
        void generateColumnMethods() {
            checkColumnName(Project.Column.projectName(), "project_name");
            checkColumnName(Project.Column.status(), "status");
        }
    }

    @Nested
    @DisplayName("handle messages declared as `PROJECTION` state and")
    class Projection {

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
    }

    @Nested
    @DisplayName("handle messages declared as `PROCESS_MANAGER` state and")
    class ProcessManager {

        @Test
        @DisplayName("generate a nested `Column` class with a private c-tor")
        void havePrivateCtor() {
            assertHasPrivateParameterlessCtor(ProjectCreation.Column.class);
        }

        @Test
        @DisplayName("generate a method which returns an `EntityColumn` for each message column")
        void generateColumnMethods() {
            checkColumnName(ProjectCreation.Column.projectName(), "project_name");
        }
    }
}
