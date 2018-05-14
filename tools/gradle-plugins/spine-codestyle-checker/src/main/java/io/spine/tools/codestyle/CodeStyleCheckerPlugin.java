/*
 * Copyright 2018, TeamDev. All rights reserved.
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
package io.spine.tools.codestyle;

import io.spine.tools.codestyle.javadoc.JavadocLinkCheckPlugin;
import io.spine.tools.codestyle.rightmargin.RightMarginCheckerPlugin;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

/**
 * The plugin that verifies code style.
 *
 * <p>The verification consists of several steps, executed in a chain during the build process.
 *
 * <p>Each step is configured with the parameters as follows:
 * <ul>
 *     <li>{@linkplain Threshold "threshold"} is a number of code style violations
 *     to consider validation passed;</li>
 *     <li>{@linkplain ReportType "reportType"} if a validation is not passed.</li>
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
 *      anotherCodeStyleChecker {
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
public class CodeStyleCheckerPlugin extends SpinePlugin {

    public static final String CODESTYLE_CHECKER_EXTENSION_NAME = "codestyleChecker";

    @Override
    public void apply(final Project project) {
        project.getExtensions()
               .create(CODESTYLE_CHECKER_EXTENSION_NAME, CodeStylePluginConfiguration.class);

        log().debug("Applying Spine Javadoc link checker plugin");
        new JavadocLinkCheckPlugin().apply(project);
        log().debug("Applying Spine Right margin wrapping checker plugin");
        new RightMarginCheckerPlugin().apply(project);
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
        final ExtensionAware codeStyleExtension =
                (ExtensionAware) project.getExtensions()
                                        .getByName(CODESTYLE_CHECKER_EXTENSION_NAME);
        return codeStyleExtension.getExtensions()
                                 .create(extensionName, StepConfiguration.class);
    }
}
