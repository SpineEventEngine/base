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

package io.spine.tools.gradle.compiler;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.compiler.validation.ValidationRulesWriter;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

import static io.spine.tools.gradle.TaskName.findTestValidationRules;
import static io.spine.tools.gradle.TaskName.findValidationRules;
import static io.spine.tools.gradle.TaskName.mergeDescriptorSet;
import static io.spine.tools.gradle.TaskName.mergeTestDescriptorSet;
import static io.spine.tools.gradle.TaskName.processResources;
import static io.spine.tools.gradle.TaskName.processTestResources;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getMainTargetGenResourcesDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSet;
import static io.spine.tools.gradle.compiler.Extension.getTestTargetGenResourcesDir;

/**
 * Finds Protobuf definitions of validation rules and creates a {@code .properties} file.
 *
 * <p>For the syntax of generated properties file please see
 * {@link io.spine.tools.compiler.validation.ValidationRulesWriter}.
 *
 * @see io.spine.tools.compiler.validation.ValidationRulesWriter
 */
public class ValidationRulesLookupPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        GradleTask mainTask = findRules(project);
        GradleTask testTask = findTestRules(project);
        _debug("Validation rules lookup phase initialized with tasks: {}, {}", mainTask, testTask);
    }

    @CanIgnoreReturnValue
    private GradleTask findRules(Project project) {
        Action<Task> mainScopeAction = mainScopeActionFor(project);
        return newTask(findValidationRules, mainScopeAction)
                .insertAfterTask(mergeDescriptorSet)
                .insertBeforeTask(processResources)
                .applyNowTo(project);
    }

    @CanIgnoreReturnValue
    private GradleTask findTestRules(Project project) {
        Action<Task> testScopeAction = testScopeActionFor(project);
        return newTask(findTestValidationRules, testScopeAction)
                .insertAfterTask(mergeTestDescriptorSet)
                .insertBeforeTask(processTestResources)
                .applyNowTo(project);
    }

    private Action<Task> mainScopeActionFor(Project project) {
        _debug("Initializing the validation lookup for the `main` source code.");
        return task -> {
            File descriptorSetFile = getMainDescriptorSet(project);
            String targetResourcesDir = getMainTargetGenResourcesDir(project);
            processDescriptorSet(descriptorSetFile, targetResourcesDir);
        };
    }

    private Action<Task> testScopeActionFor(Project project) {
        _debug("Initializing the validation lookup for the `test` source code.");
        return task -> {
            File descriptorSetFile = getTestDescriptorSet(project);
            String targetGenResourcesDir = getTestTargetGenResourcesDir(project);
            processDescriptorSet(descriptorSetFile, targetGenResourcesDir);
        };
    }

    private void processDescriptorSet(File descriptorSetFile, String targetDirectory) {
        if (!descriptorSetFile.exists()) {
            logMissingDescriptorSetFile(descriptorSetFile);
        } else {
            File targetDir = new File(targetDirectory);
            ValidationRulesWriter.processDescriptorSetFile(descriptorSetFile, targetDir);
        }
    }
}
