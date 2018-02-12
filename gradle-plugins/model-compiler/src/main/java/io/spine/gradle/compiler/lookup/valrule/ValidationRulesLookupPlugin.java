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

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.gradle.GradleTask;
import io.spine.gradle.SpinePlugin;
import io.spine.gradle.compiler.message.MessageDeclaration;
import io.spine.gradle.compiler.util.PropertiesWriter;
import io.spine.tools.proto.FileDescriptors;
import io.spine.type.TypeName;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
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
import static io.spine.gradle.compiler.message.MessageDeclarations.find;
import static io.spine.option.OptionsProto.VALIDATION_OF_FIELD_NUMBER;
import static io.spine.option.UnknownOptions.getUnknownOptionValue;
import static io.spine.option.UnknownOptions.hasUnknownOption;
import static io.spine.tools.proto.FileDescriptors.isNotGoogleProto;
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

        final Collection<FileDescriptorProto> files =
                FileDescriptors.parseAndFilter(descriptorSetPath, isNotGoogleProto());
        final Collection<MessageDeclaration> declarations = find(files, new IsValidationRule());
        writeProperties(targetGeneratedResourcesDir, declarations);
        log().debug("Validation rules lookup complete.");
    }

    private static void writeProperties(String targetGeneratedResourcesDir,
                                        Iterable<MessageDeclaration> ruleDeclarations) {
        final Map<String, String> propsMap = newHashMap();
        for (MessageDeclaration declaration : ruleDeclarations) {
            final TypeName typeName = declaration.getTypeName();
            final String ruleTargets = getUnknownOptionValue(declaration.getDescriptor(),
                                                             VALIDATION_OF_FIELD_NUMBER);
            propsMap.put(typeName.value(), ruleTargets);
        }

        log().trace("Writing the validation rules description to {}/{}.",
                    targetGeneratedResourcesDir, getValRulesPropsFileName());
        final PropertiesWriter writer = new PropertiesWriter(targetGeneratedResourcesDir,
                                                             getValRulesPropsFileName());
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
