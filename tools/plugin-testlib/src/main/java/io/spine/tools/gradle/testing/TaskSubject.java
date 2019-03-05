/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.testing;

import com.google.common.truth.BooleanSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import io.spine.tools.gradle.TaskDependencies;
import io.spine.tools.gradle.TaskName;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.gradle.api.Task;

import static com.google.common.truth.Truth.assertThat;

public final class TaskSubject extends Subject<TaskSubject, Task> {

    private TaskSubject(FailureMetadata metadata, @NullableDecl Task actual) {
        super(metadata, actual);
    }

    /**
     * Obtains factory for creating task subjects for actual values.
     */
    public static Factory<TaskSubject, Task> tasks() {
        return (TaskSubject::new);
    }

    /**
     * Creates a subject of verifying if the actual value of this subject depends on
     * the passed task.
     */
    public BooleanSubject dependsOn(Task task) {
        return assertThat(TaskDependencies.dependsOn(actual(), task));
    }

    /**
     * Creates a subject of verifying if the actual value of this subject depends on
     * the task with the passed name.
     */
    public BooleanSubject dependsOn(TaskName taskName) {
        return assertThat(TaskDependencies.dependsOn(actual(), taskName));
    }

    /**
     * Creates a subject to verify that the passed task depends on the actual value of this subject.
     */
    public BooleanSubject isDependencyOf(Task task) {
        return assertThat(TaskDependencies.dependsOn(task, actual()));
    }
}
