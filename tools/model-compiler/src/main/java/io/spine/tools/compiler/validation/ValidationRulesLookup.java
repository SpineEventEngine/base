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

package io.spine.tools.compiler.validation;

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.option.UnknownOptions;
import io.spine.tools.properties.PropertiesWriter;
import io.spine.tools.proto.MessageDeclaration;
import io.spine.type.TypeName;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static io.spine.option.OptionsProto.VALIDATION_OF_FIELD_NUMBER;
import static io.spine.tools.proto.FileDescriptors.parseSkipStandard;
import static io.spine.tools.proto.SourceFile.allThat;

/**
 * Finds Protobuf definitions of validation rules and creates a {@code .properties} file,
 * which contains entries like:
 *
 * <p>{@code foo.bar.ValidationRule=alpha.beta.TargetMessage.name_of_field_for_rule}.
 *
 * <p>If a validation rule has more than one target, the entry will look like:
 *
 * <p>{@code foo.bar.ValidationRule=foo.bar.MessageOne.field_name,foo.bar.MessageTwo.field_name}.
 *
 * @author Dmytro Grankin
 * @author Alexander Yevsyukov
 */
public final class ValidationRulesLookup {

    /** Prevents instantiation of this utility class. */
    private ValidationRulesLookup() {
    }

    public static void processDescriptorSetFile(File descriptorSetFile, File targetDir) {
        final Logger log = log();

        log.debug("Validation rules lookup started.");
        findRulesAndWriteProperties(descriptorSetFile, targetDir);
        log.debug("Validation rules lookup complete.");
    }

    private static void findRulesAndWriteProperties(File setFile, File targetDir) {
        final List<FileDescriptorProto> files = parseSkipStandard(setFile.getPath());
        final List<MessageDeclaration> declarations = allThat(files, new IsValidationRule());
        writeProperties(declarations, targetDir);
    }

    private static void writeProperties(Iterable<MessageDeclaration> ruleDeclarations,
                                        File targetDir) {
        final Map<String, String> propsMap = newHashMap();
        for (MessageDeclaration declaration : ruleDeclarations) {
            // Convert the type since `tools` uses own `TypeName` for avoiding circular dependency.
            final TypeName typeName = TypeName.of(declaration.getTypeName()
                                                             .value());
            final String ruleTargets = UnknownOptions.get(declaration.getMessage(),
                                                          VALIDATION_OF_FIELD_NUMBER);
            propsMap.put(typeName.value(), ruleTargets);
        }

        final String fileName = io.spine.validate.rules.ValidationRules.fileName();
        log().debug("Writing the validation rules description to {}/{}.",
                                                targetDir, fileName);
        final PropertiesWriter writer = new PropertiesWriter(targetDir.getAbsolutePath(), fileName);
        writer.write(propsMap);
    }

    private static class IsValidationRule implements Predicate<DescriptorProto> {

        @Override
        public boolean apply(@Nullable DescriptorProto input) {
            checkNotNull(input);
            return UnknownOptions.hasOption(input, VALIDATION_OF_FIELD_NUMBER);
        }
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ValidationRulesLookup.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
