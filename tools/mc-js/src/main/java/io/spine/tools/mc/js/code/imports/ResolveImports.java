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
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.logging.Logging;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.FileReference;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.js.fs.FileName;
import io.spine.tools.mc.js.code.GenerationTask;
import io.spine.tools.mc.js.fs.JsFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.spine.tools.mc.js.code.imports.ImportStatement.hasImport;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.toList;

/**
 * A task to resolve imports in generated files.
 *
 * <p>Supports only {@code CommonJs} imports.
 *
 * <p>The task should be performed last among {@linkplain GenerationTask generation tasks}
 * to ensure that imports won't be modified after execution of this task.
 */
public final class ResolveImports extends GenerationTask implements Logging {

    static final String GOOGLE_PROTOBUF_MODULE = "google-protobuf";

    /**
     * The relative path from the test sources directory to the main sources directory.
     *
     * <p>Depends on the structure of Spine Web project.
     */
    private static final String TEST_PROTO_RELATIVE_TO_MAIN = "../main/";

    private static final Pattern GOOGLE_PROTOBUF_MODULE_PATTERN =
            Pattern.compile(GOOGLE_PROTOBUF_MODULE + FileReference.separator());

    private final Set<ExternalModule> modules;

    public ResolveImports(Directory generatedRoot, Collection<ExternalModule> modules) {
        super(generatedRoot);
        this.modules = ImmutableSet.copyOf(modules);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        for (FileDescriptor file : fileSet.files()) {
            FileName fileName = FileName.from(file);
            _debug().log("Resolving imports in the file `%s`.", fileName);
            Path filePath = generatedRoot().resolve(fileName);
            resolveInFile(filePath);
        }
    }

    @VisibleForTesting
    void resolveInFile(Path filePath) {
        JsFile file = new JsFile(filePath);
        relativizeStandardProtoImports(file);
        resolveRelativeImports(file);
    }

    private void resolveRelativeImports(JsFile file) {
        processImports(file, new IsUnresolvedRelativeImport(), this::resolveRelativeImports);
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
            ImportStatement importStatement = new ImportStatement(line, file);
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
        String fileReference = original.path()
                                       .value();
        String relativePathToRoot = original.sourceDirectory()
                                            .relativize(generatedRoot().path())
                                            .toString();
        String replacement = relativePathToRoot.isEmpty()
                             ? FileReference.currentDirectory()
                             : relativePathToRoot.replace('\\', '/') + FileReference.separator();
        String relativeReference = GOOGLE_PROTOBUF_MODULE_PATTERN.matcher(fileReference)
                                                                 .replaceFirst(replacement);
        return original.replacePath(relativeReference);
    }

    /**
     * Attempts to resolve a relative import.
     */
    private ImportStatement resolveRelativeImports(ImportStatement resolvable) {
        Optional<ImportStatement> mainSourceImport = resolveInMainSources(resolvable);
        if (mainSourceImport.isPresent()) {
            return mainSourceImport.get();
        }
        FileReference fileReference = resolvable.path();
        for (ExternalModule module : modules) {
            if (module.provides(fileReference)) {
                FileReference fileInModule = module.fileInModule(fileReference);
                return resolvable.replacePath(fileInModule.value());
            }
        }
        return resolvable;
    }

    /**
     * Attempts to resolve a relative import among main sources.
     */
    private static Optional<ImportStatement> resolveInMainSources(ImportStatement resolvable) {
        String fileReference = resolvable.path()
                                         .value();
        String delimiter = FileReference.currentDirectory();
        int insertionIndex = fileReference.lastIndexOf(delimiter) + delimiter.length();
        String updatedReference = fileReference.substring(0, insertionIndex)
                + TEST_PROTO_RELATIVE_TO_MAIN
                + fileReference.substring(insertionIndex);
        ImportStatement updatedImport = resolvable.replacePath(updatedReference);
        return updatedImport.importedFileExists()
               ? Optional.of(updatedImport)
               : Optional.empty();
    }
}
