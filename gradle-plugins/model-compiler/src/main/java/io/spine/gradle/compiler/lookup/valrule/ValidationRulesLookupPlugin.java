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

package io.spine.gradle.compiler.lookup.valrule;

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.tools.gradle.GradleTask;
import io.spine.tools.gradle.SpinePlugin;
import io.spine.tools.properties.PropertiesWriter;
import io.spine.tools.proto.MessageDeclaration;
import io.spine.type.TypeName;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static io.spine.option.OptionsProto.VALIDATION_OF_FIELD_NUMBER;
import static io.spine.option.UnknownOptions.getUnknownOptionValue;
import static io.spine.option.UnknownOptions.hasUnknownOption;
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
import static io.spine.tools.proto.FileDescriptors.parseSkipStandard;
import static io.spine.tools.proto.SourceFile.allThat;
import static io.spine.validate.rules.ValidationRules.getValRulesPropsFileName;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Finds Protobuf definitions of validation rules and creates a {@code .properties} file,
 * which contains entries like:
 *
 * <p>{@code foo.bar.ValidationRule=alpha.beta.TargetMessage.name_of_field_for_rule}.
 *
 * <p>If a validation rule has more than one target, the entry will look like:
 *
 * <p>{@code foo.bar.ValidationRule=foo.bar.FirstMessage.field_name,foo.bar.SecondMessage.field_name}.
 *
 * @author Dmytro Grankin
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
                processDescriptorSetFile(getMainDescriptorSetPath(project),
                                         getMainTargetGenResourcesDir(project)
                );
            }
        };
    }

    private static Action<Task> testScopeActionFor(final Project project) {
        log().debug("Initializing the validation lookup for the `test` source code.");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                processDescriptorSetFile(getTestDescriptorSetPath(project),
                                         getTestTargetGenResourcesDir(project)
                );
            }
        };
    }

    private static void processDescriptorSetFile(String descriptorSetFile, String targetDir) {
        final Logger log = log();
        final File setFile = new File(descriptorSetFile);
        if (!setFile.exists()) {
            logMissingDescriptorSetFile(log, setFile);
            return;
        }

        log.debug("Validation rules lookup started.");
        findRulesAndWriteProperties(setFile, targetDir);
        log.debug("Validation rules lookup complete.");
    }

    private static void findRulesAndWriteProperties(File setFile, String targetDir) {
        final List<FileDescriptorProto> files = parseSkipStandard(setFile.getPath());
        final List<MessageDeclaration> declarations = allThat(files, new IsValidationRule());
        writeProperties(declarations, targetDir);
    }

    private static void writeProperties(Iterable<MessageDeclaration> ruleDeclarations,
                                        String targetDir) {
        final Map<String, String> propsMap = newHashMap();
        for (MessageDeclaration declaration : ruleDeclarations) {
            final TypeName typeName = declaration.getTypeName();
            final String ruleTargets = getUnknownOptionValue(declaration.getMessage(),
                                                             VALIDATION_OF_FIELD_NUMBER);
            propsMap.put(typeName.value(), ruleTargets);
        }

        log().trace("Writing the validation rules description to {}/{}.",
                    targetDir, getValRulesPropsFileName());
        final PropertiesWriter writer = new PropertiesWriter(targetDir, getValRulesPropsFileName());
        writer.write(propsMap);
    }

    private static class IsValidationRule implements Predicate<DescriptorProto> {

        @Override
        public boolean apply(@Nullable DescriptorProto input) {
            checkNotNull(input);
            return hasUnknownOption(input, VALIDATION_OF_FIELD_NUMBER);
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
