/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import org.gradle.api.Action;
import org.gradle.api.Project;

import java.util.Deque;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * A utility class for working with the Gradle project hierarchy.
 *
 * @author Dmytro Dashenkov
 */
public final class ProjectHierarchy {

    /** Prevents instantiation of this utility class. */
    private ProjectHierarchy() {
    }

    /**
     * Applies the given action to all the available projects in the hierarchy defined by the given
     * root project.
     *
     * <p>The action is applied in the breadth-first order from the root project to the subprojects.
     *
     * @param rootProject {@linkplain Project#getRootProject() the root project} of a Gradle project
     *                    hierarchy
     * @param action      the action to apply to each project in the hierarchy
     */
    public static void applyToAll(Project rootProject, Action<Project> action) {
        checkNotNull(rootProject);
        checkNotNull(action);
        checkArgument(rootProject.equals(rootProject.getRootProject()),
                      "Passed project %s is not the root project. ",
                      rootProject.getPath());
        action.execute(rootProject);
        final Deque<Project> subprojects = newLinkedList(rootProject.getSubprojects());
        while (!subprojects.isEmpty()) {
            final Project current = subprojects.poll();
            checkNotNull(current);
            action.execute(current);
            subprojects.addAll(current.getSubprojects());
        }
    }
}
