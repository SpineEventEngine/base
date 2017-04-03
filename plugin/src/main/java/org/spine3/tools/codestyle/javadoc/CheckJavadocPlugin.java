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
package org.spine3.tools.codestyle.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.gradle.SpinePlugin;

import static org.spine3.gradle.TaskName.CHECK_FQN;
import static org.spine3.gradle.TaskName.COMPILE_JAVA;
import static org.spine3.gradle.TaskName.PROCESS_RESOURCES;

/**
 * The plugin that verifies Javadoc comments.
 *
 * @author Alexander Aleksandrov
 */
public class CheckJavadocPlugin extends SpinePlugin {

    public static final String SPINE_LINK_CHECKER_EXTENSION_NAME = "checkJavadoc";

    @Override
    public void apply(final Project project) {
        project.getExtensions()
               .create(SPINE_LINK_CHECKER_EXTENSION_NAME, Extension.class);
        final FqnLinkInspection fqnLinkInspection = new FqnLinkInspection(project);
        final Action<Task> action = fqnLinkInspection.actionFor(project);
        newTask(CHECK_FQN, action).insertAfterTask(COMPILE_JAVA)
                                  .insertBeforeTask(PROCESS_RESOURCES)
                                  .applyNowTo(project);
        log().debug("Starting to check Javadocs {}", action);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(CheckJavadocPlugin.class);
    }
}
