/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import io.spine.base.EntityStateField;
import io.spine.base.EventMessageField;
import io.spine.base.SubscribableField;
import io.spine.tools.column.Project;
import io.spine.tools.column.ProjectCreated;
import io.spine.tools.column.ProjectName;
import io.spine.tools.column.ProjectView;
import io.spine.tools.protoc.given.ProjectNameField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;

@DisplayName("`ProtocPlugin`, when generating subscribable fields, should")
class FieldsTest {

    @Test
    @DisplayName("generate a nested `Field` class with a private c-tor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(Project.Field.class);
    }

    @Test
    @DisplayName("generate a method which returns a `SubscribableField` for each message field")
    void generateFieldMethods() {
        checkFieldPath(Project.Field.id(), "id");
        checkFieldPath(Project.Field.projectName(), "project_name");
        checkFieldPath(Project.Field.status(), "status");
        checkFieldPath(Project.Field.parentProject(), "parent_project");
        checkFieldPath(Project.Field.assignee(), "assignee");
    }

    @Test
    @DisplayName("expose nested fields through recursively generated nested classes")
    void generateNestedFields() {
        checkFieldPath(Project.Field.projectName().value(), "project_name.value");
    }

    @Test
    @DisplayName("generate nested classes only once in case of cyclic field references")
    void handleCyclicReferences() {
        Project.Field.ProjectField parentProjectField = Project.Field.parentProject();

        checkFieldPath(parentProjectField.parentProject(), "parent_project.parent_project");
        assertThat(parentProjectField.getClass())
                .isEqualTo(parentProjectField.parentProject().getClass());
    }

    @Test
    @DisplayName("mark event message fields as `EventMessageField`")
    void markEventMessageFields() {
        ProjectCreated.Field.ProjectIdField field = ProjectCreated.Field.id();
        assertThat(field.getClass()).isAssignableTo(EventMessageField.class);
    }

    @Test
    @DisplayName("mark entity state fields as `EntityStateField`")
    void markEntityStateFields() {
        ProjectView.Field.ProjectIdField field = ProjectView.Field.id();
        assertThat(field.getClass()).isAssignableTo(EntityStateField.class);
    }

    @Test
    @DisplayName("generate fields for a custom type according to the Model Compiler configuration")
    void markCustomTypes() {
        ProjectNameField field = ProjectName.Field.value();
        assertThat(field.getClass()).isAssignableTo(ProjectNameField.class);
    }

    private static void checkFieldPath(SubscribableField field, String expectedFieldPath) {
        assertThat(field.getField().toString()).isEqualTo(expectedFieldPath);
    }
}
