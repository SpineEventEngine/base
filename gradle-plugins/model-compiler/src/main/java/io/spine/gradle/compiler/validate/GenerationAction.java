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

package io.spine.gradle.compiler.validate;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import io.spine.tools.Indent;
import io.spine.tools.compiler.MessageTypeCache;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.compiler.Extension.getIndent;
import static io.spine.tools.gradle.compiler.Extension.isGenerateValidatingBuilders;
import static io.spine.tools.gradle.compiler.Extension.isGenerateValidatingBuildersFromClasspath;

/**
 * Gradle {@code Action} for validating builder generation.
 *
 * <p>An instance-per-scope is usually created. E.g. test sources and main source are
 * generated with different instances of this class.
 *
 * @author Illia Shepilov
 */
class GenerationAction implements Action<Task> {

    private final ValidatingBuilderGenPlugin plugin;

    /**
     * Source Gradle project.
     */
    private final Project project;

    /**
     * Path to the generated Protobuf descriptor {@code .desc} file.
     */
    private final String descriptorPath;

    /**
     * An absolute path to the folder, serving as a target
     * for the generation for the given scope.
     */
    private final String targetDirPath;

    /**
     * An absolute path to the folder, containing the {@code .proto} files for the given scope.
     */
    private final String protoSrcDirPath;

    GenerationAction(ValidatingBuilderGenPlugin parent,
                     String descriptorPath,
                     String targetDirPath,
                     String protoSrcDirPath, Project project) {
        this.plugin = parent;
        this.project = project;
        this.descriptorPath = descriptorPath;
        this.targetDirPath = targetDirPath;
        this.protoSrcDirPath = protoSrcDirPath;
    }

    @Override
    public void execute(Task task) {
        if (!isGenerateValidatingBuilders(project)) {
            return;
        }
        final File setFile = new File(descriptorPath);
        if (!setFile.exists()) {
            plugin.logMissingDescriptorSetFile(setFile);
        } else {
            final Indent indent = getIndent(project);
            processDescriptorSetFile(setFile, indent);
        }
    }

    private void processDescriptorSetFile(File setFile, Indent indent) {
        final Logger log = plugin.log();
        log.debug("Generating the validating builders from {}.", setFile);

        final boolean classpathGenEnabled =
                isGenerateValidatingBuildersFromClasspath(project);

        final MetadataAssembler assembler = new MetadataAssembler(setFile.getPath());
        final Set<VBMetadata> metadataItems = assembler.assemble();

        final MessageTypeCache messageTypeCache = assembler.getAssembledMessageTypeCache();
        final ValidatingBuilderWriter writer =
                new ValidatingBuilderWriter(targetDirPath, indent, messageTypeCache);

        final Iterable<VBMetadata> metadataToWrite = filter(classpathGenEnabled,
                                                            metadataItems);

        for (VBMetadata metadata : metadataToWrite) {
            try {
                writer.write(metadata);
            } catch (RuntimeException e) {
                final String message =
                        "Cannot generate the validating builder for " + metadata + ". ";
                log.warn(message);
                log.debug(message, e);
            }
        }
        log.debug("The validating builder generation is finished.");
    }

    private Iterable<VBMetadata> filter(boolean classpathGenEnabled,
                                        Set<VBMetadata> metadataItems) {
        final Predicate<VBMetadata> shouldWritePredicate = getPredicate(classpathGenEnabled);
        final Iterable<VBMetadata> result =
                Iterables.filter(metadataItems, shouldWritePredicate);
        return result;
    }

    private Predicate<VBMetadata> getPredicate(final boolean classpathGenEnabled) {
        final Predicate<VBMetadata> result;
        if (classpathGenEnabled) {
            result = Predicates.alwaysTrue();
        } else {
            final String rootPath = protoSrcDirPath.endsWith(File.separator)
                                    ? protoSrcDirPath
                                    : protoSrcDirPath + File.separator;
            result = new SourceProtoBelongsToModule(rootPath);
        }
        return result;
    }

    /**
     * A predicate determining if the given {@linkplain VBMetadata validating builder metadata}
     * has been collected from the source file in the specified module.
     *
     * <p>Each predicate instance requires to specify the root folder of Protobuf definitions
     * for the module. This value is used to match the given {@code VBMetadata}.
     */
    private static class SourceProtoBelongsToModule implements Predicate<VBMetadata> {

        /**
         *  An absolute path to the root folder for the {@code .proto} files in the module.
         */
        private final String rootPath;

        private SourceProtoBelongsToModule(String rootPath) {
            this.rootPath = rootPath;
        }

        @Override
        public boolean apply(@Nullable VBMetadata input) {
            checkNotNull(input);

            final String path = input.getSourceProtoFilePath();
            final File protoFile = new File(rootPath + path);
            final boolean belongsToModule = protoFile.exists();
            return belongsToModule;
        }
    }
}
