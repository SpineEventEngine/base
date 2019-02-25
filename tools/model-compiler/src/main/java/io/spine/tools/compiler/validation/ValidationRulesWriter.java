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

package io.spine.tools.compiler.validation;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import io.spine.code.properties.PropertiesWriter;
import io.spine.code.proto.FileSet;
import io.spine.logging.Logging;
import io.spine.type.MessageType;
import io.spine.type.TypeName;
import io.spine.validate.rule.ValidationRules;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Finds Protobuf definitions of validation rules and creates a {@code .properties} file,
 * which contains entries like:
 *
 * <p>{@code foo.bar.ValidationRule=alpha.beta.TargetMessage.name_of_field_for_rule}.
 *
 * <p>If a validation rule has more than one target, the entry will look like:
 *
 * <p>{@code foo.bar.ValidationRule=foo.bar.MessageOne.field_name,foo.bar.MessageTwo.field_name}.
 */
public final class ValidationRulesWriter implements Logging {

    private final FileSet files;
    private final File targetDir;

    private ValidationRulesWriter(FileSet files, File targetDir) {
        this.files = files;
        this.targetDir = targetDir;
    }

    public static void processDescriptorSetFile(File descriptorSetFile, File targetDir) {
        checkNotNull(descriptorSetFile);
        checkNotNull(targetDir);

        FileSet files = FileSet.parseAsKnownFiles(descriptorSetFile);
        ValidationRulesWriter writer =
                new ValidationRulesWriter(files, targetDir);
        writer.findRulesAndWriteProperties();
    }

    private void findRulesAndWriteProperties() {
        _debug("Validation rules lookup started through {}.", files);
        List<MessageType> declarations = findRules();
        writeProperties(declarations);
        _debug("Validation rules written to directory: {}", targetDir);
    }

    private List<MessageType> findRules() {
        List<MessageType> declarations = files.findMessageTypes(new IsValidationRule());
        _debug("Found declarations: {}", declarations.size());
        return declarations;
    }

    private void writeProperties(List<MessageType> ruleDeclarations) {
        Map<String, String> propsMap = toMap(ruleDeclarations);
        writeToFile(propsMap);
    }

    private static Map<String, String> toMap(List<MessageType> ruleDeclarations) {
        Map<String, String> propsMap = newHashMap();
        ValidationOf validationOf = new ValidationOf();
        for (MessageType declaration : ruleDeclarations) {
            TypeName typeName = declaration.name();
            String ruleTargets =
                    validationOf.valueFrom(declaration.descriptor())
                                .orElseThrow(() -> newIllegalArgumentException(declaration.name()
                                                                                          .value())
                                );
            propsMap.put(typeName.value(), ruleTargets);
        }
        return propsMap;
    }

    private void writeToFile(Map<String, String> propsMap) {
        String fileName = ValidationRules.fileName();
        _debug("Writing the validation rules description to {}/{}.",
               targetDir, fileName);
        PropertiesWriter writer = new PropertiesWriter(targetDir.getAbsolutePath(), fileName);
        writer.write(propsMap);
    }

    private static class IsValidationRule implements Predicate<DescriptorProto>, Logging {

        @Override
        public boolean test(@Nullable DescriptorProto input) {
            checkNotNull(input);
            boolean result = new ValidationOf().valueFrom(input)
                                               .isPresent();
            _debug("[IsValidationRule] Tested {} with the result of {}.", input.getName(), result);
            return result;
        }

        @Override
        public String toString() {
            return "IsValidationRule predicate over DescriptorProto";
        }
    }
}
