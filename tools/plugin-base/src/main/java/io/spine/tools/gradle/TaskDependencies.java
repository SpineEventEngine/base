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
package io.spine.tools.gradle;

import org.gradle.api.Task;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for Gradle task dependencies management.
 */
public final class TaskDependencies {

    /** Prevents instantiation of this utility class. */
    private TaskDependencies() {
    }

    /**
     * Checks whether a given Gradle task depends on another Gradle task.
     */
    public static boolean dependsOn(Task task, Task ontoTask) {
        checkNotNull(task);
        checkNotNull(ontoTask);
        return dependsOn(task, ontoTask.getName());
    }

    /**
     * Checks whether a given Gradle task depends on another Gradle task with the specified name.
     */
    public static boolean dependsOn(Task task, TaskName ontoTaskWithName) {
        checkNotNull(task);
        checkNotNull(ontoTaskWithName);
        String taskName = ontoTaskWithName.name();
        return dependsOn(task, taskName);
    }

    /**
     * As long as we are dealing with Gradle Groovy-based API, we have to use `instanceof`
     * to analyze `Object[]` values returned.
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    private static boolean dependsOn(Task task, String ontoTaskWithName) {
        Set<Object> dependsOn = task.getDependsOn();

        boolean contains = false;
        for (Object anObject : dependsOn) {
            if (anObject instanceof String) {
                contains = contains || ontoTaskWithName.equals(anObject);
            }
            if (anObject instanceof Task) {
                Task objectAsTask = (Task) anObject;
                contains = contains || ontoTaskWithName.equals(objectAsTask.getName());
            }
        }
        return contains;
    }
}
