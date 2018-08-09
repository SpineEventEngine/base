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

import com.google.common.testing.NullPointerTester;
import io.spine.tools.gradle.compiler.ErrorProneChecksExtension;
import io.spine.tools.gradle.compiler.ErrorProneChecksPlugin;
import io.spine.tools.gradle.compiler.Extension;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.junit.Before;
import org.junit.Test;

import static io.spine.tools.compiler.check.given.ProjectTaskUtil.assertCompileTasksContain;
import static io.spine.tools.compiler.check.given.ProjectTaskUtil.assertCompileTasksEmpty;
import static io.spine.tools.gradle.compiler.Severity.ERROR;
import static io.spine.tools.gradle.compiler.Severity.OFF;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Dmytro Kuzmin
 */
public class SeverityConfigurerShould {

    private Project project;
    private SeverityConfigurer configurer;
    private SeverityConfigurer configurerMock;

    @Before
    public void setUp() {
        project = newProject();
        configurer = SeverityConfigurer.initFor(project);
        configurerMock = spy(configurer);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testAllPublicStaticMethods(SeverityConfigurer.class);
        new NullPointerTester().testAllPublicInstanceMethods(configurer);
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // We use one extension and just create the other one.
    @Test
    public void configure_check_severity() {
        configureModelCompilerExtension();
        ErrorProneChecksExtension extension = configureSpineCheckExtension();
        extension.useValidatingBuilder = ERROR;
        when(configurerMock.hasErrorPronePlugin()).thenReturn(true);
        configurerMock.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // We use one extension and just create the other one.
    @Test
    public void configure_check_severity_for_all_checks() {
        Extension extension = configureModelCompilerExtension();
        extension.spineCheckSeverity = ERROR;
        configureSpineCheckExtension();
        when(configurerMock.hasErrorPronePlugin()).thenReturn(true);
        configurerMock.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    public void override_model_compiler_extension_by_error_prone_checks_extension() {
        Extension modelCompilerExtension = configureModelCompilerExtension();
        modelCompilerExtension.spineCheckSeverity = OFF;
        ErrorProneChecksExtension errorProneChecksExtension = configureSpineCheckExtension();
        errorProneChecksExtension.useValidatingBuilder = ERROR;
        when(configurerMock.hasErrorPronePlugin()).thenReturn(true);
        configurerMock.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    public void not_add_severity_args_if_error_prone_plugin_not_applied() {
        when(configurerMock.hasErrorPronePlugin()).thenReturn(false);
        configurerMock.addConfigureSeverityAction();
        checkSeverityNotConfigured();
    }

    private ErrorProneChecksExtension configureSpineCheckExtension() {
        ExtensionContainer extensions = project.getExtensions();
        ErrorProneChecksExtension extension =
                extensions.create(ErrorProneChecksPlugin.extensionName(),
                                  ErrorProneChecksExtension.class);
        return extension;
    }

    private Extension configureModelCompilerExtension() {
        ExtensionContainer extensions = project.getExtensions();
        Extension extension =
                extensions.create(ModelCompilerPlugin.extensionName(), Extension.class);
        return extension;
    }

    private void checkSeverityConfiguredToError() {
        assertCompileTasksContain(project, "-Xep:UseValidatingBuilder:ERROR");
    }

    private void checkSeverityNotConfigured() {
        assertCompileTasksEmpty(project);
    }
}
