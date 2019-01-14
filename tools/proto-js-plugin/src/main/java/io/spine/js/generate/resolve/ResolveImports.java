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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.js.Directory;
import io.spine.code.js.FileName;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.GenerationTask;
import io.spine.logging.Logging;
import org.slf4j.Logger;

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

    /**
     * The path to parent directory.
     */
    private static final String PARENT_DIR = "../";
    private static final String MODULE_RELATIVE_TO_PROTO = Strings.repeat(PARENT_DIR, 2);
    @VisibleForTesting
    static final String SRC_RELATIVE_TO_PROTO = PARENT_DIR;
    @SuppressWarnings("DuplicateStringLiteralInspection" /* Used in a different context. */)
    private static final String PROJECT_SRC_DIR = "main/";

    public ResolveImports(Directory generatedRoot) {
        super(generatedRoot);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        for (FileDescriptor file : fileSet.files()) {
            FileName fileName = FileName.from(file);
            resolveInFile(fileName);
        }
    }

    private void resolveInFile(FileName fileName) {
        List<String> lines = fileLines(fileName);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            boolean isImport = ImportSnippet.hasImport(line);
            if (isImport) {
                ImportSnippet sourceImport = new ImportSnippet(line, fileName);
                ImportSnippet updatedImport =
                        resolveImport(sourceImport, generatedRoot());
                lines.set(i, updatedImport.text());
            }
        }
        rewriteFile(fileName, lines);
    }

    /**
     * Attempts to resolve an import in the file.
     *
     * <p>In particular, replaces library-like imports by relative paths
     * if the imported file {@linkplain #belongsToModule(String, Directory) belongs}
     * to the currently processed module.
     */
    @VisibleForTesting
    static ImportSnippet resolveImport(ImportSnippet resolvable, Directory generatedRoot) {
        boolean isSpine = resolvable.isSpine();
        if (!isSpine) {
            return resolvable;
        }
        String filePath = resolvable.importedFilePath();
        if (!belongsToModule(filePath, generatedRoot)) {
            return resolvable;
        }
        String pathPrefix = SRC_RELATIVE_TO_PROTO + resolvable.fileName()
                                                              .pathToRoot();
        ImportSnippet resolved = resolvable.replacePath(pathPrefix + filePath);
        return resolved;
    }

    /**
     * Tells whether a file with the specified path belongs to the currently processed module.
     *
     * <p>The method assumes a specific file structure.
     */
    static boolean belongsToModule(String filePath, Directory generatedRoot) {
        Path modulePath = generatedRoot.getPath()
                                       .resolve(MODULE_RELATIVE_TO_PROTO);
        Path absolutePath = modulePath.resolve(PROJECT_SRC_DIR)
                                      .resolve(filePath);
        boolean presentInModule = absolutePath.toFile()
                                              .exists();
        log().debug("Checking if the file {} belongs to the module, result: {}",
                    absolutePath.normalize(), presentInModule);
        return presentInModule;
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

    private List<String> fileLines(FileName fileName) {
        Path path = generatedRoot().resolve(fileName);
        try {
            List<String> lines = Files.readAllLines(path);
            return lines;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static Logger log() {
        return Logging.get(ResolveImports.class);
    }
}
