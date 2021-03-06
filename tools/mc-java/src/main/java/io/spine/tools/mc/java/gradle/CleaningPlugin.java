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
package io.spine.tools.mc.java.gradle;

import io.spine.tools.mc.java.fs.DirectoryCleaner;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import static io.spine.tools.gradle.BaseTaskName.clean;
import static io.spine.tools.mc.java.gradle.McJavaTaskName.preClean;

/**
 * Plugin which performs additional cleanup of the Spine-generated folders.
 *
 * <p>Adds a custom `:preClean` task, which is executed before the `:clean` task.
 */
public class CleaningPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        Action<Task> preCleanAction = task -> {
            _debug().log("Pre-clean: deleting the directories.");
            DirectoryCleaner.deleteDirs(McJavaExtension.getDirsToClean(project));
        };
        GradleTask preCleanTask =
                newTask(preClean, preCleanAction)
                        .insertBeforeTask(clean)
                        .applyNowTo(project);
        _debug().log("Pre-clean phase initialized: `%s`.", preCleanTask);
    }
}
