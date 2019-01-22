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

package io.spine.tools.gradle;

import io.spine.code.generate.Indent;
import io.spine.logging.Logging;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import static io.spine.tools.gradle.SpinePlugin.resolve;

/**
 * Abstract base for code generation actions.
 */
public abstract class CodeGenerationAction implements Action<Task>, Logging {

    /**
     * The plugin which executes this task.
     */
    private final SpinePlugin plugin;

    /**
     * Source Gradle project.
     */
    private final Project project;

    /**
     * Obtains the path to the generated Protobuf descriptor {@code .desc} file.
     */
    private final Supplier<String> descriptorPath;

    /**
     * Obtains an absolute path to the folder, serving as a target
     * for the generation for the given scope.
     */
    private final Supplier<String> targetDirPath;

    /**
     * Obtains an absolute path to the folder, containing the {@code .proto} files for
     * the given scope.
     */
    private final Supplier<String> protoSrcDirPath;

    /**
     * Target directory for code generation.
     */
    private @MonotonicNonNull File targetDir;

    /**
     * Indentation for the generated code.
     */
    private @MonotonicNonNull Indent indent;

    /**
     * Creates a new instance.
     *
     * @param plugin
     *         the plugin which runs the action
     * @param project
     *         the project for which we generated the code
     * @param descriptorPath
     *         the supplier of the descriptor set file path, which (as other suppliers passed
     *         to the constructor) is dynamically evaluated when the task is executed
     * @param targetDirPath
     *         the supplier of the path of the directory for the generated sources
     * @param protoSrcDirPath
     *         the supplier of the root directory with proto sources
     */
    protected CodeGenerationAction(SpinePlugin plugin,
                                   Project project,
                                   Supplier<String> descriptorPath,
                                   Supplier<String> targetDirPath,
                                   Supplier<String> protoSrcDirPath) {
        this.plugin = plugin;
        this.project = project;
        this.descriptorPath = descriptorPath;
        this.targetDirPath = targetDirPath;
        this.protoSrcDirPath = protoSrcDirPath;
    }

    /**
     * Obtains indentation to be used for code generation.
     */
    protected final Indent indent() {
        if (indent == null) {
            indent = getIndent(project);
        }
        return indent;
    }

    /**
     * Obtains indentation configuration from the project.
     */
    protected abstract Indent getIndent(Project project);

    /**
     * Obtains the plugin which runs the code generation.
     */
    protected final SpinePlugin plugin() {
        return plugin;
    }

    /**
     * Obtains the project for which the code is generated.
     */
    protected final Project project() {
        return project;
    }

    /**
     * Obtains the descriptor set file used for code generation.
     *
     * <p>If the file does not exists, logs this fact into
     * {@linkplain SpinePlugin#logMissingDescriptorSetFile(File) plugin log} and returns empty
     * {@code Optional}.
     */
    protected final Optional<File> descriptorSetFile() {
        File file = resolve(descriptorPath);
        if (!file.exists()) {
            plugin.logMissingDescriptorSetFile(file);
            return Optional.empty();
        }
        return Optional.of(file);
    }

    /**
     * Obtains directory with source proto files.
     */
    protected final File protoSrcDir() {
        return resolve(protoSrcDirPath);
    }

    /**
     * Obtains directory under which the generated source will be placed.
     */
    protected final File targetDir() {
        if (targetDir == null) {
            targetDir = resolve(targetDirPath);
        }
        return targetDir;
    }
}
