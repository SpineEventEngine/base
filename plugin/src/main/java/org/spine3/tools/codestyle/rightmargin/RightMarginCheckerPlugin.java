/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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
package org.spine3.tools.codestyle.rightmargin;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.gradle.SpinePlugin;
import org.spine3.tools.codestyle.FileChecker;
import org.spine3.tools.codestyle.StepConfiguration;

import static org.spine3.gradle.TaskName.CHECK_RIGHT_MARGIN_WRAPPING;
import static org.spine3.gradle.TaskName.COMPILE_JAVA;
import static org.spine3.gradle.TaskName.PROCESS_RESOURCES;
import static org.spine3.tools.codestyle.CodestyleCheckerPlugin.createStepExtension;

/**
 * The plugin that checks the target project java files for lines that is longer then
 * allowed threshold.
 *
 * @author Alexander Aleksandrov
 */
public class RightMarginCheckerPlugin extends SpinePlugin {
    public static final String RIGHT_MARGIN_CHECKER_EXTENSION_NAME = "rightMarginWrappingChecker";
    public StepConfiguration configuration;

    @Override
    public void apply(Project project) {
        configuration = createStepExtension(RIGHT_MARGIN_CHECKER_EXTENSION_NAME, project);
        final FileChecker checker = new FileChecker(new RightMarginValidator(configuration));
        final Action<Task> action = checker.actionFor(project);
        newTask(CHECK_RIGHT_MARGIN_WRAPPING, action).insertAfterTask(COMPILE_JAVA)
                                                    .insertBeforeTask(PROCESS_RESOURCES)
                                                    .applyNowTo(project);
        log().debug("Starting to validate right margin wrapping {}", action);
    }

    private static Logger log() {
        return RightMarginCheckerPlugin.LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(RightMarginCheckerPlugin.class);
    }
}
