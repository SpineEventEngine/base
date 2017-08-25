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

import com.google.common.base.MoreObjects;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * A base class for Spine plugins.
 *
 * <p>Brings helper functionality to operate the Gradle build lifecycle.
 *
 * @author Alex Tymchenko
 */
public abstract class SpinePlugin implements Plugin<Project> {

    /**
     * Create a new instance of {@link GradleTask.Builder}.
     *
     * <p>NOTE: the Gradle build steps are NOT modified until
     * {@link GradleTask.Builder#applyNowTo(Project)} is invoked.
     *
     * @param name   the name for the new task
     * @param action the action to invoke during the new task processing
     * @return the instance of {@code Builder}
     * @see GradleTask.Builder#applyNowTo(Project)
     */
    protected GradleTask.Builder newTask(TaskName name, Action<Task> action) {
        final GradleTask.Builder result = new GradleTask.Builder(name, action);
        return result;
    }

    protected static void logDependingTask(Logger log,
                                           TaskName taskName,
                                           TaskName beforeTask,
                                           TaskName afterTask) {
        log.debug(
                "Adding the Gradle task {} to the lifecycle: after {}, before {}",
                taskName.getValue(),
                beforeTask.getValue(),
                afterTask.getValue());
    }

    protected static void logDependingTask(Logger log, TaskName taskName, TaskName beforeTask) {
        log.debug(
                "Adding the Gradle task {} to the lifecycle: before {}",
                taskName.getValue(),
                beforeTask.getValue()
        );
    }

    /**
     * Utility wrapper around the Gradle tasks created.
     *
     * <p>Instantiated via {@link Builder}, forces the new task to be added to
     * the Gradle build lifecycle.
     */
    protected static final class GradleTask {

        private final TaskName name;
        private final Project project;

        private GradleTask(TaskName name, Project project) {
            this.name = name;
            this.project = project;
        }

        // A part of API.
        public TaskName getName() {
            return name;
        }

        // A part of API.
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

            private final Collection<Path> inputs;

            private Builder(TaskName name, Action<Task> action) {
                this.name = name;
                this.action = action;
                inputs = newLinkedList();
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
             * @return the current instance of {@link Builder}
             */
            public Builder insertBeforeTask(TaskName target) {
                checkNotNull(target, "task after the new one");
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
             * @return the current instance of {@link Builder}
             */
            public Builder insertAfterTask(TaskName target) {
                checkNotNull(target, "task before the new one");
                this.previousTask = target;
                return this;
            }

            public Builder withInputFiles(Path... inputs) {
                checkNotNull(inputs, "task inputs");
                this.inputs.addAll(copyOf(inputs));
                return this;
            }

            /**
             * Builds an instance of {@link GradleTask} and inserts it to the project
             * build lifecycle according to the "before" and "after" tasks specified in the builder.
             *
             * @param project the target Gradle project
             * @return the newly created Gradle task
             */
            public GradleTask applyNowTo(Project project) {
                final String errMsg = "Project is not specified for the new Gradle task: ";
                checkNotNull(project, errMsg + name);

                if (followingTask == null && previousTask == null) {
                    final String exceptionMsg =
                            "Either the previous or the following task must be set.";
                    throw new IllegalStateException(exceptionMsg);
                }

                final Task newTask = project.task(name.getValue())
                                            .doLast(action);
                dependTask(newTask, project);
                addTaskIO(newTask);
                final GradleTask result = new GradleTask(name, project);
                return result;
            }

            private void dependTask(Task task, Project project) {
                if (previousTask != null) {
                    task.dependsOn(previousTask.getValue());
                }
                if (followingTask != null) {
                    final TaskContainer existingTasks = project.getTasks();
                    existingTasks.getByPath(followingTask.getValue())
                                 .dependsOn(task);

                }
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
    }
}
