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

package io.spine.tools.gradle;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskContainer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * Utility wrapper around the Gradle tasks created.
 *
 * <p>Instantiated via {@link Builder}, forces the new task to be added to
 * the Gradle build lifecycle.
 */
public final class GradleTask {

    private final Task task;
    private final TaskName name;
    private final Project project;

    private GradleTask(Task task, TaskName name, Project project) {
        this.task = task;
        this.name = name;
        this.project = project;
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

        private final Collection<Path> inputs;

        Builder(TaskName name, Action<Task> action) {
            this.name = name;
            this.action = action;
            this.inputs = newLinkedList();
        }

        /**
         * Specify a task which will follow the new one.
         *
         * <p> Once built, the new instance of {@link GradleTask} will be inserted
         * before the anchor.
         *
         * <p> NOTE: invocation of either this method or {@link #insertAfterTask(TaskName)}
         * is mandatory, as the newly created instance of {@link GradleTask} must be put to
         * a certain place in the Gradle build lifecycle.
         *
         * @param target the name of the task, serving as "before" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertBeforeTask(TaskName target) {
            checkNotNull(target, "task after the new one");
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
         * <p> NOTE: invocation of either this method or {@link #insertBeforeTask(TaskName)}
         * is mandatory, as the newly created instance of {@link GradleTask} must be put
         * to a certain place in the Gradle build lifecycle.
         *
         * @param target the name of the task, serving as "after" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertAfterTask(TaskName target) {
            checkNotNull(target, "task before the new one");
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
         * {@link #insertAfterTask(TaskName)} or {@link #insertBeforeTask(TaskName)} if it's
         * guaranteed that at least one task with such name exists. Though the fallback is
         * never handled and there is no guarantee that the task will get into
         * the Gradle task graph.
         *
         * @param target the name of the tasks, serving as "after" anchor
         * @return the current instance of {@code Builder}
         */
        public Builder insertAfterAllTasks(TaskName target) {
            checkNotNull(target, "tasks before the new one");
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
         * Adds the files and/or directories to the input dataset for the task being built.
         *
         * <p>If none of the specified file system elements are present before the task
         * execution, the task will be marked as {@code NO-SOURCE} and skipped.
         *
         * <p>Multiple invocations appends the new files to the existing ones.
         *
         * @param inputs the task input files
         * @return the current instance of {@code Builder}
         */
        public Builder withInputFiles(Path... inputs) {
            checkNotNull(inputs, "task inputs");
            this.inputs.addAll(ImmutableSet.copyOf(inputs));
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

            Task newTask = project.task(name.getValue())
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
                task.dependsOn(previousTask.getValue());
            }
            if (followingTask != null) {
                TaskContainer existingTasks = project.getTasks();
                existingTasks.getByPath(followingTask.getValue())
                             .dependsOn(task);
            }
            if (previousTaskOfAllProjects != null) {
                Project root = project.getRootProject();
                dependTaskOnAllProjects(task, root);
            }
        }

        private void dependTaskOnAllProjects(Task task, Project rootProject) {
            String prevTaskName = previousTaskOfAllProjects.getValue();
            ProjectHierarchy.applyToAll(rootProject, project -> {
                Task existingTask = project.getTasks()
                                           .findByName(prevTaskName);
                if (existingTask != null) {
                    task.dependsOn(existingTask);
                }
            });
        }

        private void addTaskIO(Task task) {
            if (!inputs.isEmpty()) {
                task.getInputs()
                    .files(inputs.toArray())
                    .skipWhenEmpty()
                    .optional()
                    .withPathSensitivity(PathSensitivity.RELATIVE);
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

    @SuppressWarnings("EqualsCalledOnEnumConstant") // for consistency
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GradleTask other = (GradleTask) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.project, other.project);
    }
}
