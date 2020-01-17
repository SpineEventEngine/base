/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.GeneratedSourceRoot.GENERATED;
import static io.spine.tools.gradle.GeneratedSourceSet.GRPC;
import static io.spine.tools.gradle.GeneratedSourceSet.JAVA;
import static io.spine.tools.gradle.GeneratedSourceSet.RESOURCES;
import static io.spine.tools.gradle.GeneratedSourceSet.SPINE;

@ExtendWith(TempDirectory.class)
@DisplayName("GeneratedSourceRoot should")
class GeneratedSourceRootTest {

    private Path projectDir;
    private GeneratedSourceRoot sourceRoot;

    @BeforeEach
    void setUp(@TempDir Path dir) throws IOException {
        dir = dir.toRealPath();
        Project project = ProjectBuilder
                .builder()
                .withProjectDir(dir.toFile())
                .build();
        projectDir = dir;
        sourceRoot = GeneratedSourceRoot.of(project);
    }

    @Test
    @DisplayName("resolve '$projectDir/generated'")
    void resolveToGenerated() {
        Path generated = projectDir.resolve(GENERATED);
        Path absoluteActual = sourceRoot.path();
        assertThat((Object) absoluteActual).isEqualTo(generated);
    }

    @Test
    @DisplayName("obtain a source set subdirectory")
    void obtainSourceSet() {
        String sourceSetName = "dysfunctional-test";
        GeneratedSourceSet sourceSet = sourceRoot.sourceSet(sourceSetName);
        assertThat(sourceSet).isNotNull();
        Path subdirectory = sourceSet.path();
        Path expectedSubdirectory = projectDir.resolve(GENERATED)
                                              .resolve(sourceSetName);
        assertThat((Object) subdirectory).isEqualTo(expectedSubdirectory);
    }

    @Nested
    @DisplayName("obtain a source set which should")
    class GeneratedSourceSetTest {

        private GeneratedSourceSet sourceSet;

        @BeforeEach
        void setUp() {
            sourceSet = sourceRoot.sourceSet("disintegration-test");
        }

        @Test
        @DisplayName("obtain `java` subdir")
        void java() {
            testSubDir(JAVA, sourceSet::java);
        }

        @Test
        @DisplayName("obtain `spine` subdir")
        void spine() {
            testSubDir(SPINE, sourceSet::spine);
        }

        @Test
        @DisplayName("obtain `grpc` subdir")
        void grpc()  {
            testSubDir(GRPC, sourceSet::grpc);
        }

        @Test
        @DisplayName("obtain `resources` subdir")
        void resources() {
            testSubDir(RESOURCES, sourceSet::resources);
        }

        private void testSubDir(String name, Supplier<Path> selector) {
            Path javaSubdir = selector.get();
            assertThat((Object) javaSubdir).isEqualTo(sourceSet.path()
                                                               .resolve(name));
        }
    }
}
