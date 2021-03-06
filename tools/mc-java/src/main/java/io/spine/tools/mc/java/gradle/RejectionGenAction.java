/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.gradle;

import com.google.common.collect.ImmutableSet;
import io.spine.base.RejectionType;
import io.spine.tools.code.Indent;
import io.spine.tools.java.code.TypeSpec;
import io.spine.tools.java.code.TypeSpecWriter;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.RejectionsFile;
import io.spine.code.proto.SourceProtoBelongsToModule;
import io.spine.tools.mc.java.rejection.RThrowableSpec;
import io.spine.tools.gradle.CodeGenerationAction;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.flogger.LazyArgs.lazy;
import static io.spine.code.proto.RejectionsFile.findAll;

/**
 * Generates source code of rejections.
 *
 * <p>For each message type declared in the {@code rejections.proto} generates a corresponding
 * rejection type which extends {@link io.spine.base.RejectionThrowable RejectionThrowable} and
 * encloses an instance of the corresponding proto message.
 *
 * <p>The {@link McJavaExtension#generatedMainRejectionsDir} and
 * {@link McJavaExtension#generatedTestRejectionsDir} options allow to customize the target root
 * directory for code generation.
 *
 * <p>The {@link McJavaExtension#indent} option sets the indentation of the generated source files.
 */
final class RejectionGenAction extends CodeGenerationAction {

    RejectionGenAction(Project project,
                       Supplier<FileSet> files,
                       Supplier<String> targetDirPath,
                       Supplier<String> protoSrcDirPath) {
        super(project, files, targetDirPath, protoSrcDirPath);
    }

    @Override
    public void execute(Task task) {
        ImmutableSet<RejectionsFile> rejectionFiles = findModuleRejections(protoFiles().get());
        _debug().log("Processing the file descriptors for the rejections `%s`.",
                     rejectionFiles);
        for (RejectionsFile source : rejectionFiles) {
            // We are sure that this is a rejections file because we got them filtered.
            generateRejections(source);
        }
    }

    /**
     * Obtains all rejection files belonging to the currently processed module.
     */
    private ImmutableSet<RejectionsFile> findModuleRejections(FileSet allFiles) {
        ImmutableSet<RejectionsFile> allRejections = findAll(allFiles);
        ImmutableSet<RejectionsFile> moduleRejections = allRejections
                .stream()
                .filter(new SourceProtoBelongsToModule(protoSrcDir()))
                .collect(toImmutableSet());
        return moduleRejections;
    }

    private void generateRejections(RejectionsFile source) {
        List<RejectionType> rejections = source.rejectionDeclarations();
        if (rejections.isEmpty()) {
            return;
        }
        logGeneratingForFile(source);
        for (RejectionType rejectionType : rejections) {
            // The name of the generated `ThrowableMessage` will be the same
            // as for the Protobuf message.
            _debug().log("Processing rejection `%s`.", rejectionType.simpleJavaClassName());

            TypeSpec spec = new RThrowableSpec(rejectionType);
            TypeSpecWriter writer = new TypeSpecWriter(spec, indent());
            writer.write(targetDir().toPath());
        }
    }

    private void logGeneratingForFile(RejectionsFile source) {
        _debug().log(
                "Generating rejections from the file: `%s` " +
                        "`javaPackage`: `%s`, `javaOuterClassName`: `%s`.",
                source.path(),
                lazy(() -> PackageName.resolve(source.descriptor().toProto())),
                lazy(() -> SimpleClassName.outerOf(source.descriptor()))
        );
    }

    @Override
    protected Indent getIndent(Project project) {
        return McJavaExtension.getIndent(project);
    }
}
