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

package io.spine.tools.compiler.check;

import com.google.common.annotations.VisibleForTesting;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.invocation.Gradle;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.check.ProjectUtils.addArgsToJavaCompile;

/**
 * A helper that sets up and configures the preprocessor dependency for the {@link Project}.
 *
 * @author Dmytro Kuzmin
 */
public class PreprocessorConfigurer {

    static final String PREPROCESSOR_CONFIG_NAME = "annotationProcessor";

    @VisibleForTesting
    static final String PREPROCESSOR_ARG = "-processorpath";

    private final Project project;

    private PreprocessorConfigurer(Project project) {
        this.project = project;
    }

    /**
     * Create the {@code PreprocessorConfigurer} instance for the given project.
     *
     * @param project the project
     * @return the {@code PreprocessorConfigurer} instance
     */
    public static PreprocessorConfigurer initFor(Project project) {
        checkNotNull(project);
        return new PreprocessorConfigurer(project);
    }

    /**
     * Creates the {@code annotationProcessor} config for the project if it does not exist.
     *
     * <p>In the newer Gradle versions ({@code 4.6} and above) the config most probably already
     * exists.
     *
     * @return the {@code annotationProcessor} configuration of the project
     */
    public Configuration setupPreprocessorConfig() {
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration preprocessorConfig = configurations.findByName(PREPROCESSOR_CONFIG_NAME);
        if (preprocessorConfig == null) {
            preprocessorConfig = configurations.create(PREPROCESSOR_CONFIG_NAME);
        }
        return preprocessorConfig;
    }

    /**
     * Makes sure the given configuration is added to the preprocessor path of all the
     * {@code JavaCompile} tasks of the project.
     *
     * <p>The action is executed on the {@code projectEvaluated} stage.
     */
    public void addConfigurePreprocessorAction(Configuration preprocessorConfig) {
        checkNotNull(preprocessorConfig);
        Action<Gradle> configurePreprocessor =
                configurePreprocessorAction(preprocessorConfig);
        Gradle gradle = project.getGradle();
        gradle.projectsEvaluated(configurePreprocessor);
    }

    /**
     * Converts the {@link #configurePreprocessor(Configuration)} method to the Gradle
     * {@link Action}.
     */
    private Action<Gradle>
    configurePreprocessorAction(Configuration preprocessorConfig) {
        return gradle -> configurePreprocessor(preprocessorConfig);
    }

    /**
     * Adds the given preprocessor configuration as the preprocessor path to all the
     * {@code JavaCompile} tasks of the project.
     */
    @SuppressWarnings("TypeMayBeWeakened") // More specific type expresses the method intent better.
    private void configurePreprocessor(Configuration preprocessorConfig) {
        addArgsToJavaCompile(project, PREPROCESSOR_ARG, preprocessorConfig.getAsPath());
    }
}
