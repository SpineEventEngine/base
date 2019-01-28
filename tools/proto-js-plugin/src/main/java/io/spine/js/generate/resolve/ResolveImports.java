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

package io.spine.js.generate.resolve;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.js.Directory;
import io.spine.code.js.FileName;
import io.spine.code.js.FileReference;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.GenerationTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * A task to resolve imports in generated files.
 *
 * <p>Supports only {@code CommonJs} imports.
 *
 * <p>The task should be performed last.
 */
public final class ResolveImports extends GenerationTask {

    private final List<ExternalModule> modules;

    public ResolveImports(Directory generatedRoot, List<ExternalModule> modules) {
        super(generatedRoot);
        this.modules = ImmutableList.copyOf(modules);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        for (FileDescriptor file : fileSet.files()) {
            FileName fileName = FileName.from(file);
            resolveInFile(fileName);
        }
    }

    private void resolveInFile(FileName fileName) {
        Path filePath = generatedRoot().resolve(fileName);
        List<String> lines = fileLines(filePath);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            boolean isImport = ImportSnippet.hasImport(line);
            if (isImport) {
                ImportSnippet sourceImport = new ImportSnippet(line, filePath.toFile());
                ImportSnippet updatedImport = resolveImport(sourceImport);
                lines.set(i, updatedImport.text());
            }
        }
        rewriteFile(fileName, lines);
    }

    /**
     * Attempts to resolve an import in the file.
     */
    private ImportSnippet resolveImport(ImportSnippet resolvable) {
        FileReference fileReference = resolvable.path();
        if (!fileReference.isRelative()) {
            return resolvable;
        }
        for (ExternalModule module : modules) {
            if (module.provides(fileReference)) {
                FileReference pathInModule = module.pathInModule(fileReference);
                return resolvable.replacePath(pathInModule.value());
            }
        }
        return resolvable;
    }

    private void rewriteFile(FileName fileName, Iterable<String> lines) {
        Path filePath = generatedRoot().resolve(fileName)
                                       .toAbsolutePath();
        try {
            Files.write(filePath, lines, Charsets.UTF_8, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static List<String> fileLines(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            return lines;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
