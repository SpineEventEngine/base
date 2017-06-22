/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package io.spine.gradle;

import org.gradle.api.Task;

import java.util.Set;

/**
 * Utilities for Gradle task dependencies management.
 *
 * @author Alex Tymchenko
 */
public class TaskDependencies {

    private TaskDependencies() {
    }

    /**
     * Checks whether a given Gradle task depends on another Gradle task.
     */
    public static boolean dependsOn(Task task, Task ontoTask) {
        return dependsOn(task, ontoTask.getName());
    }

    /**
     * Checks whether a given Gradle task depends on another Gradle task with the specified name.
     */
    public static boolean dependsOn(Task task, TaskName ontoTaskWithName) {
        final String taskName = ontoTaskWithName.getValue();
        return dependsOn(task, taskName);
    }

    /**
     * As long as we are dealing with Gradle Groovy-based API, we have to use `instanceof`
     * to analyze `Object[]` values returned.
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    private static boolean dependsOn(Task task, String ontoTaskWithName) {
        final Set<Object> dependsOn = task.getDependsOn();

        boolean contains = false;
        for (Object anObject : dependsOn) {
            if (anObject instanceof String) {
                contains = contains || ontoTaskWithName.equals(anObject);
            }
            if (anObject instanceof Task) {
                final Task objectAsTask = (Task) anObject;
                contains = contains || ontoTaskWithName.equals(objectAsTask.getName());
            }
        }
        return contains;
    }
}
