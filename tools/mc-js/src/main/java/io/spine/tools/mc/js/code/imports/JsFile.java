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

package io.spine.tools.mc.js.code.imports;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.FileReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.io.Files2.checkExists;
import static io.spine.tools.mc.js.code.imports.ImportStatement.hasImport;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.toList;

/**
 * A JavaScript file present on a file system.
 */
public final class JsFile {

    static final String GOOGLE_PROTOBUF_MODULE = "google-protobuf";

    /**
     * The relative path from the test sources directory to the main sources directory.
     *
     * <p>Depends on the structure of Spine Web project.
     */
    private static final String TEST_PROTO_RELATIVE_TO_MAIN = "../main/";

    private static final Pattern GOOGLE_PROTOBUF_MODULE_PATTERN =
            Pattern.compile(GOOGLE_PROTOBUF_MODULE + FileReference.separator());

    @VisibleForTesting
    static final String EXTENSION = ".js";

    private final Path path;

    /**
     * Creates a new instance.
     *
     * @param path
     *         the path to existing JavaScript file
     */
    public JsFile(Path path) {
        checkNotNull(path);
        String fileName = path.toString();
        checkArgument(fileName.endsWith(EXTENSION),
                      "A JavaScript file is expected. Passed: `%s`.", fileName);
        this.path = checkExists(path);
    }

    /**
     * Obtains the path to the file.
     */
    public Path path() {
        return path;
    }

    public void resolveImports(Set<ExternalModule> modules) {
        relativizeStandardProtoImports(this);
        resolveRelativeImports(this, modules);
    }

    private void resolveRelativeImports(JsFile file, Set<ExternalModule> modules) {
        processImports(file, new IsUnresolvedRelativeImport(),
                       statement -> JsFile.resolveRelativeImports(statement, modules));
    }

    /**
     * Processes import statements in this file.
     *
     * <p>Rewrites the file using the updated imports.
     *
     * @param importFilter
     *         the predicate to filter out imports to be processed
     * @param processFunction
     *         the function processing an import
     */
    public static void processImports(JsFile file,
                                      java.util.function.Predicate<ImportStatement> importFilter,
                                      ProcessImport processFunction) {
        try (Stream<String> lines = Files.lines(file.path())) {
            List<String> updatedLines = lines
                    .map(line -> processLine(file, line, importFilter, processFunction))
                    .collect(toList());
            rewriteFile(file.path(), updatedLines);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static String processLine(JsFile jsFile,
                                      String line,
                                      java.util.function.Predicate<ImportStatement> importFilter,
                                      ProcessImport processFunction) {
        File file = jsFile.path().toFile();
        if (hasImport(line)) {
            ImportStatement importStatement = new ImportStatement(file, line);
            boolean matchesFilter = importFilter.test(importStatement);
            if (matchesFilter) {
                return processFunction.apply(importStatement)
                                      .text();
            }
        }
        return line;
    }

    private static void rewriteFile(Path path, Iterable<String> lines) {
        try {
            Files.write(path, lines, Charsets.UTF_8, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Replaces all imports from {@code google-protobuf} module by relative imports.
     * Then, these imports are resolved among external modules.
     *
     * <p>Such a replacement is required since we want to use own versions
     * of standard types, which are additionally processed by the Protobuf JS plugin.
     *
     * <p>The custom versions of standard Protobuf types are provided by
     * the {@linkplain ExternalModule#spineWeb() Spine Web}.
     *
     * @param file
     *         the file to process imports in
     */
    private void relativizeStandardProtoImports(JsFile file) {
        processImports(file, new IsGoogleProtobufImport(), this::relativizeStandardProtoImport);
    }

    private ImportStatement relativizeStandardProtoImport(ImportStatement original) {
        String fileReference = original.fileRef()
                                       .value();
        String relativePathToRoot = original.sourceDirectory()
                                            .relativize(generatedRoot().path())
                                            .toString();
        String replacement = relativePathToRoot.isEmpty()
                             ? FileReference.currentDirectory()
                             : relativePathToRoot.replace('\\', '/') + FileReference.separator();
        String relativeReference = GOOGLE_PROTOBUF_MODULE_PATTERN.matcher(fileReference)
                                                                 .replaceFirst(replacement);
        return original.replaceRef(relativeReference);
    }

    /**
     * Attempts to resolve a relative import.
     */
    private static ImportStatement resolveRelativeImports(ImportStatement resolvable,
                                                          Set<ExternalModule> modules) {
        Optional<ImportStatement> mainSourceImport = resolveInMainSources(resolvable);
        if (mainSourceImport.isPresent()) {
            return mainSourceImport.get();
        }
        FileReference fileReference = resolvable.fileRef();
        for (ExternalModule module : modules) {
            if (module.provides(fileReference)) {
                FileReference fileInModule = module.fileInModule(fileReference);
                return resolvable.replaceRef(fileInModule.value());
            }
        }
        return resolvable;
    }

    /**
     * Attempts to resolve a relative import among main sources.
     */
    private static Optional<ImportStatement> resolveInMainSources(ImportStatement resolvable) {
        String fileReference = resolvable.fileRef()
                                         .value();
        String delimiter = FileReference.currentDirectory();
        int insertionIndex = fileReference.lastIndexOf(delimiter) + delimiter.length();
        String updatedReference = fileReference.substring(0, insertionIndex)
                + TEST_PROTO_RELATIVE_TO_MAIN
                + fileReference.substring(insertionIndex);
        ImportStatement updatedImport = resolvable.replaceRef(updatedReference);
        return updatedImport.importedFileExists()
               ? Optional.of(updatedImport)
               : Optional.empty();
    }
}
