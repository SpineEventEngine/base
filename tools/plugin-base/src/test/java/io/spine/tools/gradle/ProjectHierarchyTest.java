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

package io.spine.tools.gradle;

import com.google.common.testing.NullPointerTester;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.gradle.testing.NoOp;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.tools.gradle.ProjectHierarchy.applyToAll;
import static io.spine.tools.gradle.testing.NoOp.action;

@DisplayName("`ProjectHierarchy` should")
class ProjectHierarchyTest extends UtilityClassTest<ProjectHierarchy> {

    ProjectHierarchyTest() {
        super(ProjectHierarchy.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(Project.class, newProject("some-name"))
              .setDefault(Action.class, NoOp.action());
    }

    @Test
    @DisplayName("traverse hierarchy in bf ordering")
    void traverseHierarchyInBfOrdering() {
        Project root = newProject("root");

        Project sub1 = withParent(root, "sub1");
        Project sub2 = withParent(root, "sub2");

        Project subsub1 = withParent(sub1, "subsub1");
        Project subsub2 = withParent(sub1, "subsub2");

        Set<Project> visited = newHashSet();
        ProjectHierarchy.applyToAll(root, project -> {
            assertThat(visited).doesNotContain(project);
            for (Project child : project.getSubprojects()) {
                assertThat(visited).doesNotContain(child);
            }
            visited.add(project);
        });

        assertThat(visited).containsExactly(root, sub1, sub2, subsub1, subsub2);
        assertThat(visited).hasSize(5);
    }

    @Test
    @DisplayName("not accept non root projects")
    void notAcceptNonRootProjects() {
        Project project = newProject("root");
        Project sub = withParent(project, "sub");
        assertIllegalArgument(() -> applyToAll(sub, action()));
    }

    private static Project newProject(String name) {
        return ProjectBuilder.builder()
                             .withName(name)
                             .build();
    }

    private static Project withParent(Project parent, String name) {
        return ProjectBuilder.builder()
                             .withName(name)
                             .withParent(parent)
                             .build();
    }
}