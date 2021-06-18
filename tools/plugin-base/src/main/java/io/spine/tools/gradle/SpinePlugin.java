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
package io.spine.tools.gradle;

import io.spine.logging.Logging;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.function.Supplier;

import static io.spine.io.Files2.toAbsolute;

/**
 * Abstract base for Gradle plugins introduced by Spine Event Engine framework.
 *
 * <p>Brings helper functionality to operate the Gradle build lifecycle.
 */
public abstract class SpinePlugin implements Plugin<Project>, Logging {

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
        GradleTask.Builder result = new GradleTask.Builder(name, action);
        return result;
    }

    /**
     * Resolves an absolute file name obtained as a string from the passed supplier.
     */
    public static File resolve(Supplier<String> path) {
        String pathname = path.get();
        File result = toAbsolute(pathname);
        return result;
    }

    protected final void logMissingDescriptorSetFile(File setFile) {
        _debug().log(
                "Missing descriptor set file `%s`.%n" +
                        "Please enable descriptor set generation.%n" +
                        "See: " +
                        "https://github.com/google/protobuf-gradle-plugin/blob/master/README.md" +
                        "#generate-descriptor-set-files",
                setFile.getPath()
        );
    }
}
