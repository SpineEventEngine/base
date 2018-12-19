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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.tools.compiler.validation.ValidationRulesLookup;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

import static io.spine.tools.gradle.TaskName.FIND_TEST_VALIDATION_RULES;
import static io.spine.tools.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.tools.gradle.TaskName.MERGE_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.MERGE_TEST_DESCRIPTOR_SET;
import static io.spine.tools.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.tools.gradle.TaskName.PROCESS_TEST_RESOURCES;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getMainTargetGenResourcesDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestTargetGenResourcesDir;

/**
 * Finds Protobuf definitions of validation rules and creates a {@code .properties} file.
 *
 * <p>For the syntax of generated properties file please see {@link ValidationRulesLookup}.
 *
 * @see ValidationRulesLookup
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
        return newTask(FIND_VALIDATION_RULES, mainScopeAction)
                .insertAfterTask(MERGE_DESCRIPTOR_SET)
                .insertBeforeTask(PROCESS_RESOURCES)
                .applyNowTo(project);
    }

    @CanIgnoreReturnValue
    private GradleTask findTestRules(Project project) {
        Action<Task> testScopeAction = testScopeActionFor(project);
        return newTask(FIND_TEST_VALIDATION_RULES, testScopeAction)
                .insertAfterTask(MERGE_TEST_DESCRIPTOR_SET)
                .insertBeforeTask(PROCESS_TEST_RESOURCES)
                .applyNowTo(project);
    }

    private Action<Task> mainScopeActionFor(Project project) {
        _debug("Initializing the validation lookup for the `main` source code.");
        return task -> {
            String descriptorSetFile = getMainDescriptorSetPath(project);
            String targetResourcesDir = getMainTargetGenResourcesDir(project);
            processDescriptorSet(descriptorSetFile, targetResourcesDir);
        };
    }

    private Action<Task> testScopeActionFor(Project project) {
        _debug("Initializing the validation lookup for the `test` source code.");
        return task -> {
            String descriptorSetPath = getTestDescriptorSetPath(project);
            String targetGenResourcesDir = getTestTargetGenResourcesDir(project);
            processDescriptorSet(descriptorSetPath, targetGenResourcesDir);
        };
    }

    private void processDescriptorSet(String descriptorSetFile, String targetDirectory) {
        File setFile = new File(descriptorSetFile);
        if (!setFile.exists()) {
            logMissingDescriptorSetFile(setFile);
        } else {
            File targetDir = new File(targetDirectory);
            ValidationRulesLookup.processDescriptorSetFile(setFile, targetDir);
        }
    }
}
