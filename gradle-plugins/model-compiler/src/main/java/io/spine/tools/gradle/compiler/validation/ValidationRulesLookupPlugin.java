/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.gradle.compiler.validation;

import io.spine.tools.compiler.validation.ValidationRulesLookup;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.io.File;

import static io.spine.tools.gradle.TaskName.FIND_TEST_VALIDATION_RULES;
import static io.spine.tools.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.tools.gradle.TaskName.GENERATE_PROTO;
import static io.spine.tools.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.tools.gradle.TaskName.PROCESS_TEST_RESOURCES;
import static io.spine.tools.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getMainTargetGenResourcesDir;
import static io.spine.tools.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.tools.gradle.compiler.Extension.getTestTargetGenResourcesDir;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Finds Protobuf definitions of validation rules and creates a {@code .properties} file.
 *
 * <p>For the syntax of generated properties file please see {@link ValidationRulesLookup}.
 *
 * @author Dmytro Grankin
 * @see ValidationRulesLookup
 */
public class ValidationRulesLookupPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        logDependingTask(log(), FIND_VALIDATION_RULES, PROCESS_RESOURCES, GENERATE_PROTO);
        final Action<Task> mainScopeAction = mainScopeActionFor(project);
        final GradleTask findRules = newTask(FIND_VALIDATION_RULES,
                                             mainScopeAction).insertAfterTask(GENERATE_PROTO)
                                                             .insertBeforeTask(PROCESS_RESOURCES)
                                                             .applyNowTo(project);
        logDependingTask(log(), FIND_TEST_VALIDATION_RULES, PROCESS_TEST_RESOURCES,
                         GENERATE_TEST_PROTO);
        final Action<Task> testScopeAction = testScopeActionFor(project);
        final GradleTask findTestRules =
                newTask(FIND_TEST_VALIDATION_RULES,
                        testScopeAction).insertAfterTask(GENERATE_TEST_PROTO)
                                        .insertBeforeTask(PROCESS_TEST_RESOURCES)
                                        .applyNowTo(project);
        log().debug("Validation rules lookup phase initialized with tasks: {}, {}",
                    findRules, findTestRules);
    }

    private static Action<Task> mainScopeActionFor(final Project project) {
        log().debug("Initializing the validation lookup for the `main` source code.");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String descriptorSetFile = getMainDescriptorSetPath(project);
                final String targetResourcesDir = getMainTargetGenResourcesDir(project);
                processDescriptorSet(descriptorSetFile, targetResourcesDir);
            }
        };
    }

    private static Action<Task> testScopeActionFor(final Project project) {
        log().debug("Initializing the validation lookup for the `test` source code.");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                final String descriptorSetPath = getTestDescriptorSetPath(project);
                final String targetGenResourcesDir = getTestTargetGenResourcesDir(project);
                processDescriptorSet(descriptorSetPath, targetGenResourcesDir);
            }
        };
    }

    private static void processDescriptorSet(String descriptorSetFile, String targetDirectory) {
        final File setFile = new File(descriptorSetFile);
        if (!setFile.exists()) {
            logMissingDescriptorSetFile(log(), setFile);
        } else {
            final File targetDir = new File(targetDirectory);
            ValidationRulesLookup.processDescriptorSetFile(setFile, targetDir);
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = getLogger(ValidationRulesLookupPlugin.class);
    }
}
