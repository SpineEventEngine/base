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
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static io.spine.tools.compiler.check.DependencyConfigurer.SPINE_CHECKER_MODULE;
import static io.spine.tools.compiler.check.DependencyConfigurer.SPINE_TOOLS_GROUP;
import static io.spine.tools.compiler.check.PreprocessorConfigurer.PREPROCESSOR_CONFIG_NAME;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
// We ignore boolean "success" flag which is not interesting for us in this test.
public class DependencyConfigurerShould {

    private static final String STUB_VERSION = "versionStub";

    private Configuration preprocessor;
    private DependencyConfigurer helper;
    private DependencyConfigurer helperMock;

    @Before
    public void setUp() {
        Project project = newProject();
        ConfigurationContainer configs = project.getConfigurations();
        preprocessor = configs.getByName(PREPROCESSOR_CONFIG_NAME);
        helper = DependencyConfigurer.createFor(project, preprocessor);
        helperMock = spy(helper);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testAllPublicStaticMethods(DependencyConfigurer.class);
        new NullPointerTester().testAllPublicInstanceMethods(helper);
    }

    @Test
    public void add_spine_check_dependency_to_annotation_processor_config() {
        when(helperMock.acquireModelCompilerVersion()).thenReturn(Optional.of(STUB_VERSION));
        when(helperMock.isChecksVersionResolvable(any())).thenReturn(true);

        helperMock.addErrorProneChecksDependency();

        boolean hasDependency = hasErrorProneChecksDependency();
        assertTrue(hasDependency);
    }

    @Test
    public void not_add_spine_check_dependency_if_it_is_not_resolvable() {
        when(helperMock.acquireModelCompilerVersion()).thenReturn(Optional.of(STUB_VERSION));
        when(helperMock.isChecksVersionResolvable(any())).thenReturn(false);

        helperMock.addErrorProneChecksDependency();

        boolean hasDependency = hasErrorProneChecksDependency();
        assertFalse(hasDependency);
    }

    @Test
    public void not_add_spine_check_dependency_if_model_compiler_dependency_not_available() {
        when(helperMock.acquireModelCompilerVersion()).thenReturn(Optional.empty());

        helperMock.addErrorProneChecksDependency();

        boolean hasDependency = hasErrorProneChecksDependency();
        assertFalse(hasDependency);
    }

    private boolean hasErrorProneChecksDependency() {
        DependencySet dependencies = preprocessor.getDependencies();
        for (Dependency dependency : dependencies) {
            if (SPINE_TOOLS_GROUP.equals(dependency.getGroup()) &&
                    SPINE_CHECKER_MODULE.equals(dependency.getName())) {
                return true;
            }
        }
        return false;
    }
}
