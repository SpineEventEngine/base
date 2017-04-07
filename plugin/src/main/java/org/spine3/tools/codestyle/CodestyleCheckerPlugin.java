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
 * <p>The verification consists of several steps, executed in a chain during the build process.
 *
 * <p>Each step is configured with the parameters as follows:
 * <ul>
 *     <li>{@linkplain Threshold "threshold"} is a number of code style violations
 *     to consider check passed;</li>
 *     <li>{@linkplain ReportType "reportType"} if a check is not passed.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * codestyleChecker {
 *
 *      javadocLinkChecker {
 *
 *          // Will report the broken FQN links if there is at least one.
 *          threshold = 0
 *
 *          // The violations are logged to the build report.
 *          reportType = "warn"
 *      }
 *
 *      anotherCodestyleChecker {
 *
 *          // Will report the code style violations if their number exceeds five.
 *          threshold = 5
 *
 *          // If threshold is exceeded, the build will fail.
 *          // Its report will contain the violations.
 *          reportType = "error"
 *      }
 * }
 * }</pre>
 *
 * @author Alexander Aleksandrov
 * @author Dmytro Grankin
 * @see Threshold
 * @see ReportType
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

    /**
     * Creates {@linkplain StepConfiguration step extension}
     * with the specified name in the specified project.
     *
     * @param extensionName the extension name to create
     * @param project       the project in which to create extensions
     * @return the created extension
     */
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
