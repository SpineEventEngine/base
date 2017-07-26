/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.lookup.valrule;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.gradle.SpinePlugin;
import io.spine.gradle.compiler.util.DescriptorSetUtil.IsNotGoogleProto;
import io.spine.gradle.compiler.util.PropertiesWriter;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.spine.gradle.TaskName.FIND_TEST_VALIDATION_RULES;
import static io.spine.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.gradle.TaskName.PROCESS_RESOURCES;
import static io.spine.gradle.TaskName.PROCESS_TEST_RESOURCES;
import static io.spine.gradle.compiler.Extension.getMainDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getMainTargetGenResourcesDir;
import static io.spine.gradle.compiler.Extension.getTestDescriptorSetPath;
import static io.spine.gradle.compiler.Extension.getTestTargetGenResourcesDir;
import static io.spine.gradle.compiler.util.DescriptorSetUtil.getProtoFileDescriptors;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Finds Protobuf definitions of validation rules and creates a {@code .properties} file,
 * which contains entries like:
 *
 * <p>{@code VALIDATION_RULE_TYPE=MESSAGE_TYPE_FOR_VALIDATION_RULE.FIELD_NAME}
 *
 * @author Dmytro Grankin
 */
public class ValidationRulesLookupPlugin extends SpinePlugin {

    @VisibleForTesting
    static final String VALIDATION_PROPS_FILE_NAME = "validation_rules.properties";

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
                findValidationRulesAndWriteProps(getMainTargetGenResourcesDir(project),
                                                 getMainDescriptorSetPath(project));
            }
        };
    }

    private static Action<Task> testScopeActionFor(final Project project) {
        log().debug("Initializing the validation lookup for the `test` source code.");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                findValidationRulesAndWriteProps(getTestTargetGenResourcesDir(project),
                                                 getTestDescriptorSetPath(project));
            }
        };
    }

    private static void findValidationRulesAndWriteProps(String targetGeneratedResourcesDir,
                                                         String descriptorSetPath) {
        log().debug("Validation rules lookup started.");

        final Map<String, String> propsMap = newHashMap();
        final IsNotGoogleProto protoFilter = new IsNotGoogleProto();
        final Collection<FileDescriptorProto> files = getProtoFileDescriptors(descriptorSetPath,
                                                                              protoFilter);
        for (FileDescriptorProto file : files) {
            final Map<String, String> rules = new ValidationRulesFinder(file).findRules();
            propsMap.putAll(rules);
        }
        if (propsMap.isEmpty()) {
            log().debug("Validation rules lookup complete. No rules found.");
            return;
        }

        log().trace("Writing the validation rules description to {}/{}.",
                    targetGeneratedResourcesDir, VALIDATION_PROPS_FILE_NAME);
        final PropertiesWriter writer = new PropertiesWriter(targetGeneratedResourcesDir,
                                                             VALIDATION_PROPS_FILE_NAME);
        writer.write(propsMap);

        log().debug("Validation rules lookup complete.");
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
