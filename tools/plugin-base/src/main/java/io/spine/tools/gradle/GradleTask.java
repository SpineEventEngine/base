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

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.tasks.TaskContainer;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Utility wrapper around the Gradle tasks created.
 *
 * <p>Instantiated via {@link Builder}, forces the new task to be added to
 * the Gradle build lifecycle.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") /* Instantiated via Builder. */
public final class GradleTask {

    private final Task task;
    private final TaskName name;
    private final Project project;

    private GradleTask(Task task, TaskName name, Project project) {
        this.task = task;
        this.name = name;
        this.project = project;
    }

    /** Creates a new instance from the specified {@code Task}. */
    public static GradleTask from(Task task) {
        checkNotNull(task);
        TaskName taskName = new DynamicTaskName(task.getName());
        Project project = task.getProject();
        return new GradleTask(task, taskName, project);
    }

    /** Obtains the Gradle task itself. */
    public Task getTask() {
        return task;
    }

    /** Obtains task name. */
    public TaskName getName() {
        return name;
    }

    /** Obtains the project of the task. */
    public Project getProject() {
        return project;
    }

    /**
     * A builder for {@link GradleTask}.
     *
     * <p>NOTE: unlike most classes following the {@code Builder} pattern,
     * this one provides {@link #applyNowTo(Project)} method instead of
     * {@code build(..)}. This is done to add some additional semantics to
     * such an irreversible action like this.
     */
    public static final class Builder {
        private final TaskName name;
        private final Action<Task> action;

        private TaskName followingTask;
        private TaskName previousTask;
        private TaskName previousTaskOfAllProjects;

        private boolean allowNoDependencies;

        private @MonotonicNonNull UnionFileCollection inputs;
        private Map<String, @Nullable Object> inputProperties;
        private @MonotonicNonNull UnionFileCollection outputs;

        Builder(TaskName name, Action<Task> action) {
            this.name = name;
            this.action = action;
        }

        /**
         * Specify a task which will follow the new one.
         *
         * <p> Once built, the new instance of {@link GradleTask} will be inserted
         * before the anchor.
         *
         * <p> NOTE: invocation of either this method or {@link #insertAfterTask} is mandatory,
         * as the newly created instance of {@link GradleTask} must be put to
         * a certain place in the Gradle build lifecycle.
         *
         * @param target the name of the task, serving as "before" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertBeforeTask(TaskName target) {
            checkNotNull(target, "Task after the new one");
            checkState(dependenciesRequired());
            this.followingTask = target;
            return this;
        }

        /**
         * Specify a task which will precede the new one.
         *
         * <p> Once built, the new instance of {@link GradleTask} will be inserted
         * after the anchor.
         *
         * <p> NOTE: invocation of either this method or {@link #insertBeforeTask} is mandatory,
         * as the newly created instance of {@link GradleTask} must be put
         * to a certain place in the Gradle build lifecycle.
         *
         * @param target the name of the task, serving as "after" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertAfterTask(TaskName target) {
            checkNotNull(target, "Task before the new one");
            checkState(dependenciesRequired());
            this.previousTask = target;
            return this;
        }

        /**
         * Inserts tasks which will precede the new one.
         *
         * <p>Unlike {@link #insertAfterTask insertAfterTask()}, this method will depend
         * the new task on <b>every</b> task with such name in the project (i.e. the tasks of
         * the root project and all the subprojects).
         *
         * <p>If a certain project does not have a task with the specified name, no action is
         * performed for that project.
         *
         * <p>This method does not guarantee that the task will be included into a standard
         * Gradle build.
         *
         * <p>Invocation of this method may substitute the invocation of
         * {@link #insertAfterTask} or {@link #insertBeforeTask} if it's guaranteed that at least
         * one task with such name exists. Though the fallback is never handled and there is
         * no guarantee that the task will get into the Gradle task graph.
         *
         * @param target the name of the tasks, serving as "after" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertAfterAllTasks(TaskName target) {
            checkNotNull(target, "Tasks before the new one");
            checkState(dependenciesRequired());
            this.previousTaskOfAllProjects = target;
            return this;
        }

        /**
         * States that the task dependencies will be added to the task later.
         *
         * <p>If this method is not called, the dependencies <strong>must</strong> be specified
         * via this builder.
         */
        public Builder allowNoDependencies() {
            checkState(previousTask == null);
            checkState(previousTaskOfAllProjects == null);
            checkState(followingTask == null);
            allowNoDependencies = true;
            return this;
        }

        /**
         * Adds the files and/or directories to the input file set for the task being built.
         *
         * <p>If none of the specified file system elements are present before the task
         * execution, the task will be marked as {@code NO-SOURCE} and skipped.
         *
         * <p>Multiple invocations appends the new files to the existing ones.
         *
         * @param inputs the task input files
         * @return the current instance of {@code Builder}
         */
        public Builder withInputFiles(FileCollection inputs) {
            checkNotNull(inputs, "Task inputs");
            if (this.inputs == null) {
                this.inputs = new UnionFileCollection();
            }
            this.inputs.addToUnion(inputs);
            return this;
        }

        /**
         * Adds a task input property.
         *
         * <p>An input property is treated in a similar way as
         * an {@linkplain #withInputFiles input file}.
         *
         * <p>Multiple invocations of this method append new properties. If there already is
         * a property with is such a name, the value is overridden.
         *
         * @param propertyName
         *         the name of the property
         * @param value
         *         the value of the property
         * @return the current instance of {@code Builder}
         */
        public Builder withInputProperty(String propertyName, @Nullable Serializable value) {
            checkNotNull(propertyName);
            if (inputProperties == null) {
                inputProperties = newHashMap();
            }
            inputProperties.put(propertyName, value);
            return this;
        }

        /**
         * Adds the files and/or directories to the output file set for the task being built.
         *
         * <p>If all the files listed as output do not change since the previous run of the task,
         * the task will be marked as {@code UP-TO-DATE} and skipped.
         *
         * <p>Note that a task is not skipped if its {@link #withInputFiles inputs} are changes.
         *
         * @param outputs the task output files
         * @return the current instance of {@code Builder}
         */
        public Builder withOutputFiles(FileCollection outputs) {
            checkNotNull(outputs, "Task outputs");
            if (this.outputs == null) {
                this.outputs = new UnionFileCollection();
            }
            this.outputs.addToUnion(outputs);
            return this;
        }

        /**
         * Builds an instance of {@link GradleTask} and inserts it to the project
         * build lifecycle according to the "before" and "after" tasks specified in the builder.
         *
         * @param project the target Gradle project
         * @return the newly created Gradle task
         */
        @CanIgnoreReturnValue
        public GradleTask applyNowTo(Project project) {
            String errMsg = "Project is not specified for the new Gradle task: ";
            checkNotNull(project, errMsg + name);

            if (dependenciesRequired() && !dependenciesPresent()) {
                String exceptionMsg = "Either the previous or the following task must be set. " +
                        "Call `allowNoDependencies()` to skip task dependencies setup.";
                throw new IllegalStateException(exceptionMsg);
            }

            Task newTask = project.task(name.name())
                                  .doLast(action);
            dependTask(newTask, project);
            addTaskIO(newTask);
            GradleTask result = new GradleTask(newTask, name, project);
            return result;
        }

        private boolean dependenciesRequired() {
            return !allowNoDependencies;
        }

        private boolean dependenciesPresent() {
            return followingTask != null
                || previousTask != null
                || previousTaskOfAllProjects != null;
        }

        private void dependTask(Task task, Project project) {
            if (previousTask != null) {
                task.dependsOn(previousTask.name());
            }
            if (followingTask != null) {
                TaskContainer existingTasks = project.getTasks();
                existingTasks.getByPath(followingTask.name())
                             .dependsOn(task);
            }
            if (previousTaskOfAllProjects != null) {
                Project root = project.getRootProject();
                dependTaskOnAllProjects(task, root);
            }
        }

        private void dependTaskOnAllProjects(Task task, Project rootProject) {
            String prevTaskName = previousTaskOfAllProjects.name();
            ProjectHierarchy.applyToAll(rootProject, project -> {
                Task existingTask = project.getTasks()
                                           .findByName(prevTaskName);
                if (existingTask != null) {
                    task.dependsOn(existingTask);
                }
            });
        }

        private void addTaskIO(Task task) {
            if (inputs != null) {
                task.getInputs()
                    .files(inputs)
                    .skipWhenEmpty()
                    .optional();
            }
            if (inputProperties != null) {
                task.getInputs()
                    .properties(inputProperties);
            }
            if (outputs != null) {
                task.getOutputs()
                    .files(outputs)
                    .optional();
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", name)
                          .add("project", project)
                          .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, project);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GradleTask other = (GradleTask) obj;
        return Objects.equals(this.name.name(), other.name.name())
                && Objects.equals(this.project, other.project);
    }
}
