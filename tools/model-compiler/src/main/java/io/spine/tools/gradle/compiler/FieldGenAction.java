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

import io.spine.code.gen.Indent;
import io.spine.code.proto.FileSet;
import io.spine.tools.gradle.CodeGenerationAction;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.function.Supplier;

/**
 * Does the generation of helper interfaces for declaring columns.
 *
 * <p>For all top-level message types that declare columns and conform to the entity with columns
 * requirements (see the {@code (column)} option), a helper {@code XWithColumns} interface is
 * generated, where {@code X} is the message type name. See {@link io.spine.base.EntityWithColumns}
 * for details.
 *
 * <p>The {@link Extension#targetGenColumnsRootDir} and
 * {@link Extension#targetTestGenColumnsRootDir} options allow to customize the target directory
 * for code generation.
 *
 * <p>The {@link Extension#indent} option customizes the indentation of the generated source files.
 */
final class FieldGenAction extends CodeGenerationAction {

    FieldGenAction(Project project,
                   Supplier<FileSet> files,
                   Supplier<String> targetDirPath,
                   Supplier<String> protoSrcDirPath) {
        super(project, files, targetDirPath, protoSrcDirPath);
    }

    @Override
    public void execute(Task task) {
        // Do nothing for now.
    }

    @Override
    protected Indent getIndent(Project project) {
        return Extension.getIndent(project);
    }
}
