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

package io.spine.generate.dart;

import com.google.common.collect.ImmutableMap;
import io.spine.code.fs.js.FileReference;
import io.spine.tools.code.ExternalModule;
import io.spine.tools.gradle.ProtoDartTaskName;
import io.spine.tools.gradle.SourceScope;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Copy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.spine.tools.gradle.BaseTaskName.assemble;
import static io.spine.tools.gradle.ProtoDartTaskName.copyGeneratedDart;
import static io.spine.tools.gradle.ProtoDartTaskName.copyTestGeneratedDart;
import static io.spine.tools.gradle.ProtoDartTaskName.resolveImports;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.ProtocPluginName.dart;
import static io.spine.tools.gradle.SourceScope.main;
import static io.spine.tools.gradle.SourceScope.test;
import static java.lang.String.format;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static org.gradle.api.Task.TASK_TYPE;

/**
 * A Gradle plugin which configures Protobuf Dart code generation.
 *
 * <p>Generates mapping between Protobuf type URLs and Dart types and reflective descriptors
 * (a.k.a. {@code BuilderInfo}s).
 *
 * @see DartProtocConfigurationPlugin
 */
public final class ProtoDartPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        Extension extension = new Extension(project);
        extension.register();

        Plugin<Project> protocConfig = new DartProtocConfigurationPlugin();
        protocConfig.apply(project);

        createMainCopyTask(project, extension);
        createTestCopyTask(project, extension);
        createResolveImportTask(project, extension);
    }

    private static void createMainCopyTask(Project project, Extension extension) {
        createCopyTask(project, extension, main);
    }

    private static void createTestCopyTask(Project project, Extension extension) {
        createCopyTask(project, extension, test);
    }

    private static void createCopyTask(Project project, Extension extension, SourceScope scope) {
        ProtoDartTaskName taskName;
        DirectoryProperty targetDir;
        TaskName runAfter;
        if (scope == main) {
            taskName = copyGeneratedDart;
            targetDir = extension.getLibDir();
            runAfter = generateProto;
        } else {
            taskName = copyTestGeneratedDart;
            targetDir = extension.getTestDir();
            runAfter = generateTestProto;
        }
        Copy task = (Copy) project.task(ImmutableMap.of(TASK_TYPE, Copy.class), taskName.name());
        task.from(extension.getGeneratedBaseDir()
                           .dir(scope.name() + File.separator + dart.name()));
        task.into(targetDir);
        task.dependsOn(runAfter.name());
        project.getTasks()
               .getByName(assemble.name())
               .dependsOn(taskName.name());
    }

    private void createResolveImportTask(Project project, Extension extension) {
        newTask(resolveImports, task -> {
            FileTree generatedDir = extension.getMainGeneratedDir()
                                             .getAsFileTree();
            generatedDir.forEach(file -> resolveImports(file, extension));
        })
                .insertAfterTask(copyGeneratedDart)
                .insertBeforeTask(assemble)
                .applyNowTo(project);
    }

    private void resolveImports(File sourceFile, Extension extension) {
        if (!isPbDartFile(sourceFile)) {
            return;
        }
        List<String> lines;
        Path asPath = sourceFile.toPath();
        try {
            lines = readAllLines(asPath);
        } catch (IOException e) {
            throw new GradleException(format("Unable to read file `%s`.", sourceFile), e);
        }
        List<ExternalModule> modules = extension.modules();
        Pattern importPattern = Pattern.compile("import \"(.+)\" as (.+);");
        List<String> resultLines = new ArrayList<>(lines.size());
        for (String line : lines) {
            Matcher matcher = importPattern.matcher(line);
            if (matcher.find()) {
                String path = matcher.group(1);
                Path absolutePath = asPath.resolve(path)
                                          .normalize();
                Path libPath = extension.getLibDir()
                                        .getAsFile()
                                        .map(File::toPath)
                                        .get();
                Path relativeImport = libPath.relativize(absolutePath);
                FileReference reference = FileReference.of(relativeImport.toString());
                for (ExternalModule module : modules) {
                    if (module.provides(reference)) {
                        String importStatement = format("import \"package:%s/%s\" as %s;",
                                                        module.name(),
                                                        relativeImport,
                                                        matcher.group(2));
                        resultLines.add(importStatement);
                    }
                }
            } else {
                resultLines.add(line);
            }
        }
        try {
            write(asPath, resultLines);
        } catch (IOException e) {
            throw new GradleException(format("Unable to write file `%s`.", sourceFile), e);
        }
    }

    private static boolean isPbDartFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName();
        return name.endsWith(".pb.dart")
                || name.endsWith(".pbenum.dart")
                || name.endsWith(".pbservice.dart")
                || name.endsWith(".pbjson.dart");
    }
}
