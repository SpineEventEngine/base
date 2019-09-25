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

package io.spine.dart.generate;

import io.spine.dart.knowntypes.TypesTemplate;
import io.spine.io.Resource;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

import static io.spine.tools.gradle.TaskName.assemble;
import static io.spine.tools.gradle.TaskName.testClasses;

public final class ProtoDartPlugin extends SpinePlugin {

    private static final Resource TEMPLATE = Resource.file("types.template.dart");

    @Override
    public void apply(Project project) {
        Extension extension = new Extension(project);
        extension.registerIn(project);
        newTask(TaskName.generateDartTypeRegistry, createAction(extension))
                .insertBeforeTask(assemble)
                .withOutputFiles(project.files(extension.getMainTypeRegistry()))
                .applyNowTo(project);
        newTask(TaskName.generateDartTestTypeRegistry, createTestAction(extension))
                .insertBeforeTask(testClasses)
                .withOutputFiles(project.files(extension.getTestTypeRegistry()))
                .applyNowTo(project);
        Plugin<Project> protocConfig = new DartProtocConfigurationPlugin();
        protocConfig.apply(project);
    }

    private static Action<Task> createAction(Extension extension) {
        return t -> generateTypeRegistry(extension.packageName(),
                                         extension.mainDescriptorSetFile(),
                                         extension.mainTypeRegistryFile());
    }

    private static Action<Task> createTestAction(Extension extension) {
        return t -> generateTypeRegistry(extension.packageName(),
                                         extension.testDescriptorSetFile(),
                                         extension.testTypeRegistryFile());
    }

    private static void
    generateTypeRegistry(String packageName, File descriptorsFile, File targetFile) {
        TypesTemplate typesTemplate = TypesTemplate.instance(TEMPLATE, descriptorsFile);
        typesTemplate.fillInForPackage(packageName);
        typesTemplate.storeAsFile(targetFile);
    }
}
