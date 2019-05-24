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

import com.google.common.testing.NullPointerTester;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.tools.compiler.check.DependencyConfigurer.SPINE_CHECKER_MODULE;
import static io.spine.tools.gradle.Artifact.SPINE_TOOLS_GROUP;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
// We ignore boolean "success" flag which is not interesting for us in this test.
@DisplayName("DependencyConfigurer should")
class DependencyConfigurerTest {

    private static final String STUB_VERSION = "versionStub";

    private Configuration annotationProcessorConfig;
    private DependencyConfigurer helper;
    private DependencyConfigurer helperMock;

    @BeforeEach
    void setUp() {
        Project project = newProject();
        ConfigurationContainer configs = project.getConfigurations();
        annotationProcessorConfig = configs.getByName(annotationProcessor.value());
        helper = DependencyConfigurer.createFor(project, annotationProcessorConfig);
        helperMock = spy(helper);
    }

    @Test
    @DisplayName("pass null tolerance check")
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicStaticMethods(DependencyConfigurer.class);
        new NullPointerTester().testAllPublicInstanceMethods(helper);
    }

    @Test
    @DisplayName("add spine check dependency to annotation processor config")
    void addSpineCheckDependencyToAnnotationProcessorConfig() {
        when(helperMock.acquireModelCompilerVersion()).thenReturn(Optional.of(STUB_VERSION));
        when(helperMock.isChecksVersionResolvable(any())).thenReturn(true);

        helperMock.addErrorProneChecksDependency();

        boolean hasDependency = hasErrorProneChecksDependency();
        assertTrue(hasDependency);
    }

    @Test
    @DisplayName("not add spine check dependency if it is not resolvable")
    void notAddSpineCheckDependencyIfItIsNotResolvable() {
        when(helperMock.acquireModelCompilerVersion()).thenReturn(Optional.of(STUB_VERSION));
        when(helperMock.isChecksVersionResolvable(any())).thenReturn(false);

        helperMock.addErrorProneChecksDependency();

        boolean hasDependency = hasErrorProneChecksDependency();
        assertFalse(hasDependency);
    }

    @Test
    @DisplayName("not add spine check dependency if model compiler dependency not available")
    void notAddSpineCheckDependencyIfModelCompilerDependencyNotAvailable() {
        when(helperMock.acquireModelCompilerVersion()).thenReturn(Optional.empty());

        helperMock.addErrorProneChecksDependency();

        boolean hasDependency = hasErrorProneChecksDependency();
        assertFalse(hasDependency);
    }

    private boolean hasErrorProneChecksDependency() {
        DependencySet dependencies = annotationProcessorConfig.getDependencies();
        for (Dependency dependency : dependencies) {
            if (SPINE_TOOLS_GROUP.equals(dependency.getGroup()) &&
                    SPINE_CHECKER_MODULE.equals(dependency.getName())) {
                return true;
            }
        }
        return false;
    }
}
