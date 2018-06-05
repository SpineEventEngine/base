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

package io.spine.tools.gradle.compiler;

import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.of;
import static io.spine.tools.gradle.TaskName.COMPILE_JAVA;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;

/**
 * @author Dmytro Dashenkov
 */
public class CompileConfiguration extends SpinePlugin {

    @Override
    public void apply(Project project) {
        configureGeneratedJavaCompilation(project);
    }

    private static void configureGeneratedJavaCompilation(Project project) {
        Task generatedCompile = createCompilationTask(project);
        configureStandardCompile(project, generatedCompile);
    }

    private static Task createCompilationTask(Project project) {
        JavaCompile task = (JavaCompile) project.task(of("type", JavaCompile.class),
                                                      "compileGeneratedJava");
        task.dependsOn(GENERATE_PROTO.getValue());
        SourceSet mainJava = project.getExtensions()
                                    .getByType(SourceSetContainer.class)
                                    .findByName("main");
        checkNotNull(mainJava);
        task.source(mainJava);
        task.include(GeneratedJavaFile.in(project));
        task.setDestinationDir(project.getBuildDir()
                                      .toPath()
                                      .resolve("generatedClasses")
                                      .toFile());
        return task;
    }

    private static void configureStandardCompile(Project project, Task compileGenerated) {
        Task standardCompile = project.getTasks()
                                      .findByName(COMPILE_JAVA.getValue());
        checkNotNull(standardCompile);
        standardCompile.dependsOn(compileGenerated);

        JavaCompile standardJavaCompile = (JavaCompile) standardCompile;
        standardJavaCompile.exclude(GeneratedJavaFile.in(project));
        FileCollection generatedClasses = compileGenerated.getOutputs()
                                                          .getFiles();
        standardJavaCompile.getClasspath()
                           .add(generatedClasses);
    }

    private static final class GeneratedJavaFile implements Spec<FileTreeElement> {

        private static Spec<FileTreeElement> in(Project project) {
            return new GeneratedJavaFile();
        }

        @Override
        public boolean isSatisfiedBy(FileTreeElement file) {
        // TODO:2018-06-05:dmytro.dashenkov: Use generated base dir from Extension.
            return file.toString().contains("/generated/");
        }
    }
}
