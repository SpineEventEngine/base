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
package org.spine3.tools.codestyle;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.gradle.SpinePlugin;
import org.spine3.tools.codestyle.javadoc.JavadocLinkCheckerPlugin;

/**
 * The plugin that verifies code style.
 *
 * @author Alexander Aleksandrov
 */
public class CodestyleCheckerPlugin extends SpinePlugin {

    public static final String CODESTYLE_CHECKER_EXTENSION_NAME = "codestyleChecker";

    @Override
    public void apply(final Project project) {
        project.getExtensions()
               .create(CODESTYLE_CHECKER_EXTENSION_NAME, CodestylePluginConfiguration.class);

        log().debug("Applying Spine Javadoc link checker plugin");
        new JavadocLinkCheckerPlugin().apply(project);
    }

    public static StepConfiguration createStepExtension(String extensionName, Project project) {
        final ExtensionAware codestyleExtension =
                (ExtensionAware) project.getExtensions()
                                        .getByName(CODESTYLE_CHECKER_EXTENSION_NAME);
        return codestyleExtension.getExtensions()
                                 .create(extensionName, StepConfiguration.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(CodestyleCheckerPlugin.class);
    }
}
