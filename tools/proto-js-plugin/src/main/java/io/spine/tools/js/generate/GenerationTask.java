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

package io.spine.tools.js.generate;

import io.spine.tools.js.fs.Directory;
import io.spine.tools.js.fs.FileName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.ProtoBelongsToModule;
import io.spine.code.proto.SourceFile;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A code generation task for Protobuf declarations.
 *
 * <p>The class operates on the provided set of Protobuf files
 * and enhances their JavaScript counterparts by generating additional code.
 */
public abstract class GenerationTask {

    private final Directory generatedRoot;

    protected GenerationTask(Directory generatedRoot) {
        this.generatedRoot = checkNotNull(generatedRoot);
    }

    /**
     * Generates code for the specified file set.
     *
     * @param fileSet
     *         the Protobuf files to generate code for
     */
    public final void performFor(FileSet fileSet) {
        checkNotNull(fileSet);
        FileSet filtered = filter(fileSet);
        if (hasFiles(filtered)) {
            generateFor(filtered);
        }
    }

    /**
     * Filters out files that should not processed by the task.
     *
     * <p>The method filters out files that don't belong to the module.
     * A file is considered belonging to the module if it was compiled to JavaScript.
     * A {@code .js} file is checked instead of an original {@code .proto} file
     * since a module can expose Protobufs defined by other modules and these files
     * still should be handled.
     *
     * @param fileSet
     *         the files to filter
     * @return the files to perform the taks for
     */
    protected FileSet filter(FileSet fileSet) {
        ProtoBelongsToModule predicate = new CompiledProtoBelongsToModule(generatedRoot);
        return fileSet.filter(predicate.forDescriptor());
    }

    /**
     * Generates code for the Protobuf files.
     */
    protected abstract void generateFor(FileSet fileSet);

    /**
     * Obtains the root of the generated Protobuf sources.
     */
    protected Directory generatedRoot() {
        return generatedRoot;
    }

    /**
     * Checks if the task has any files to process.
     *
     * <p>Will return {@code false} either if there are no known types to process or the generated
     * files for them cannot be found.
     *
     * @return {@code true} if there are files to process and {@code false} otherwise
     */
    private boolean hasFiles(FileSet fileSet) {
        boolean hasFilesToProcess = !fileSet.isEmpty() && generatedRoot.exists();
        return hasFilesToProcess;
    }

    /**
     * A predicate determining if the given Protobuf file was compiled to JavaScript
     * and belongs to the specified module.
     */
    private static final class CompiledProtoBelongsToModule extends ProtoBelongsToModule {

        private final Directory generatedRoot;

        /**
         * Creates a new instance.
         *
         * @param generatedRoot
         *         the root directory for generated Protobufs
         */
        private CompiledProtoBelongsToModule(Directory generatedRoot) {
            super();
            checkNotNull(generatedRoot);
            this.generatedRoot = generatedRoot;
        }

        @Override
        protected Path resolve(SourceFile file) {
            FileName fileName = FileName.from(file.descriptor());
            Path filePath = generatedRoot.resolve(fileName);
            return filePath;
        }
    }
}
