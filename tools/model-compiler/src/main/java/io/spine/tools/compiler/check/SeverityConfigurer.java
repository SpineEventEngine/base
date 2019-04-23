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

package io.spine.tools.compiler.check;

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.gradle.compiler.Severity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.PluginContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.check.ProjectArguments.addArgsToJavaCompile;
import static io.spine.tools.gradle.compiler.ErrorProneChecksExtension.getUseValidatingBuilder;
import static io.spine.tools.gradle.compiler.Extension.getSpineCheckSeverity;

/**
 * The helper for the Spine-custom Error Prone checks configuration of the {@link Project}.
 *
 * <p>This class cannot configure the check severities without the Error Prone plugin applied to
 * the project.
 *
 * @see io.spine.tools.gradle.compiler.ErrorProneChecksExtension
 * @see io.spine.tools.gradle.compiler.Extension#getSpineCheckSeverity(Project)
 */
public class SeverityConfigurer {

    private static final String ERROR_PRONE_PLUGIN_ID = "net.ltgt.errorprone";

    private final Project project;

    private SeverityConfigurer(Project project) {
        this.project = project;
    }

    /**
     * Create the {@code SeverityConfigurer} instance for the given project.
     *
     * @param project the project
     * @return the {@code SeverityConfigurer} instance
     */
    public static SeverityConfigurer initFor(Project project) {
        checkNotNull(project);
        return new SeverityConfigurer(project);
    }

    /**
     * Adds the action configuring Spine Error Prone check severities to the
     * {@code projectEvaluated} stage of the project.
     */
    public void addConfigureSeverityAction() {
        Action<Gradle> configureCheckSeverity = configureSeverityAction();
        Gradle gradle = project.getGradle();
        gradle.projectsEvaluated(configureCheckSeverity);
    }

    /**
     * Converts the {@link #configureCheckSeverity()} method to the Gradle {@link Action}.
     */
    private Action<Gradle> configureSeverityAction() {
        return gradle -> configureCheckSeverity();
    }

    /**
     * Adds command line flags necessary to configure Spine Error Prone check severities to all
     * {@code JavaCompile} tasks of the project.
     */
    private void configureCheckSeverity() {
        if (!hasErrorPronePlugin()) {
            log().debug("Cannot configure Spine Error Prone check severity as the Error Prone " +
                                "plugin is not applied to the project {}.", project.getName());
            return;
        }
        Severity defaultSeverity = getSpineCheckSeverity(project);
        configureUseValidatingBuilder(defaultSeverity);
    }

    /**
     * Checks if the project has the Error Prone plugin applied.
     */
    @VisibleForTesting
    boolean hasErrorPronePlugin() {
        PluginContainer appliedPlugins = project.getPlugins();
        boolean hasPlugin = appliedPlugins.hasPlugin(ERROR_PRONE_PLUGIN_ID);
        return hasPlugin;
    }

    /**
     * Configures the "UseValidatingBuilder" check severity for all {@code JavaCompile} tasks of
     * the project.
     *
     * <p>Uses default severity set in the {@code modelCompiler} extension if set and not
     * overridden by the more specific {@code spineErrorProneChecks} extension.
     */
    private void configureUseValidatingBuilder(@Nullable Severity defaultSeverity) {
        Severity severity = getUseValidatingBuilder(project);
        if (severity == null) {
            if (defaultSeverity == null) {
                return;
            } else {
                severity = defaultSeverity;
            }
        }
        log().debug("Setting UseValidatingBuilder checker severity to {} for the project {}",
                    severity.name(), project.getName());
        String severityArg = "-Xep:UseValidatingBuilder:" + severity.name();
        addArgsToJavaCompile(project, severityArg);
    }

    /**
     * Obtains the SLF4J logger specific to this class.
     *
     * @return the logger for this class
     */
    private static Logger log() {
        return LoggerFactory.getLogger(SeverityConfigurer.class);
    }
}
