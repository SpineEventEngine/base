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

package io.spine.tools.mc.js.fs;

import com.google.common.collect.ImmutableList;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.FileReference;
import io.spine.logging.Logging;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An import line extracted from a source file for being
 * {@linkplain #resolve(Path, ImmutableList) resolved}.
 */
final class ImportStatement implements Logging {

    private static final String GOOGLE_PROTOBUF_MODULE = "google-protobuf";
    private static final Pattern GOOGLE_PROTOBUF_MODULE_PATTERN =
            Pattern.compile(GOOGLE_PROTOBUF_MODULE + FileReference.separator());

    private static final String IMPORT_START = "require('";
    private static final String IMPORT_END = "')";

    /**
     * The relative path from the test sources directory to the main sources directory.
     *
     * <p>Depends on the structure of Spine Web project.
     */
    private static final String TEST_PROTO_RELATIVE_TO_MAIN = "../main/";

    private final JsFile file;
    private final String text;

    /**
     * Creates a new instance.
     *
     * @param file
     *         the file which declares the import statement
     * @param text
     *         the text of the statement
     */
    ImportStatement(JsFile file, String text) {
        checkArgument(
                declaredIn(text),
                "An import statement should contain: `%s ... %s`.", IMPORT_START, IMPORT_END
        );
        checkNotNull(file);
        this.text = text;
        this.file = file;
    }

    /**
     * Tells whether the line contains an import statement.
     */
    static boolean declaredIn(String line) {
        return line.contains(IMPORT_START);
    }

    /**
     * Replaces the import from {@code google-protobuf} module by a relative import.
     * Then, the import is resolved among external modules.
     *
     * <p>Such a replacement is required since we want to use own versions
     * of standard types, which are additionally processed by the Model Compiler for JS.
     *
     * <p>The custom versions of standard Protobuf types are provided by
     * the {@linkplain ExternalModule#spineWeb() Spine Web}.
     */
    ImportStatement resolve(Path generatedRoot, ImmutableList<ExternalModule> modules) {
        ImportStatement resolved = this;
        if (containsGoogleProtobufType()) {
            resolved = relativizeStandardProtoImport(generatedRoot);
        }
        if (resolved.isUnresolvedRelativeImport()) {
            resolved = resolved.resolveRelativeTo(modules);
        }
        return resolved;
    }

    /**
     * Tells if this statement reference a standard Protobuf type
     * ({@code google-protobuf/google/protobuf/..}).
     */
    private boolean containsGoogleProtobufType() {
        return fileRef().value().startsWith(GOOGLE_PROTOBUF_MODULE + "/google/protobuf/");
    }

    /**
     * Tells if this statement refers to a file which cannot be found on the file system.
     */
    private boolean isUnresolvedRelativeImport() {
        boolean isRelative = fileRef().isRelative();
        boolean fileDoesNotExist = !importedFileExists();
        return isRelative && fileDoesNotExist;
    }

    /**
     * Attempts to resolve a relative import.
     */
    private ImportStatement resolveRelativeTo(ImmutableList<ExternalModule> modules) {
        Optional<ImportStatement> mainSourceImport = resolveInMainSources();
        if (mainSourceImport.isPresent()) {
            return mainSourceImport.get();
        }
        FileReference fileReference = fileRef();
        for (ExternalModule module : modules) {
            if (module.provides(fileReference)) {
                FileReference fileInModule = module.fileInModule(fileReference);
                return replaceRef(fileInModule.value());
            }
        }
        return this;
    }

    /**
     * Attempts to resolve a relative import among main sources.
     */
    private Optional<ImportStatement> resolveInMainSources() {
        String fileReference = fileRef().value();
        String delimiter = FileReference.currentDirectory();
        int insertionIndex = fileReference.lastIndexOf(delimiter) + delimiter.length();
        String updatedReference = fileReference.substring(0, insertionIndex)
                + TEST_PROTO_RELATIVE_TO_MAIN
                + fileReference.substring(insertionIndex);
        ImportStatement updatedImport = replaceRef(updatedReference);
        return updatedImport.importedFileExists()
               ? Optional.of(updatedImport)
               : Optional.empty();
    }

    private ImportStatement relativizeStandardProtoImport(Path generatedRoot) {
        String fileReference = fileRef().value();
        String relativePathToRoot =
                sourceDirectory()
                        .relativize(generatedRoot)
                        .toString();
        String replacement = relativePathToRoot.isEmpty()
                             ? FileReference.currentDirectory()
                             : relativePathToRoot.replace('\\', '/') + FileReference.separator();
        String relativeReference = GOOGLE_PROTOBUF_MODULE_PATTERN.matcher(fileReference)
                                                                 .replaceFirst(replacement);
        return replaceRef(relativeReference);
    }

    /**
     * Obtains the file reference used in this import.
     */
    public FileReference fileRef() {
        int beginIndex = text.indexOf(IMPORT_START) + IMPORT_START.length();
        int endIndex = text.indexOf(IMPORT_END, beginIndex);
        String importPath = text.substring(beginIndex, endIndex);
        return FileReference.of(importPath);
    }

    /**
     * Obtains a new instance with the updated path in the import statement.
     */
    public ImportStatement replaceRef(CharSequence newFileRef) {
        String updatedText = text.replace(fileRef().value(), newFileRef);
        return new ImportStatement(file, updatedText);
    }

    /**
     * Obtains the text of the import.
     */
    @Override
    public String toString() {
        return text;
    }

    /**
     * Tells whether the imported file is present on a file system.
     */
    private boolean importedFileExists() {
        Path filePath = importedFilePath();
        boolean exists = filePath.toFile()
                                 .exists();
        _debug().log("Checking if the imported file `%s` exists, result: %b.", filePath, exists);
        return exists;
    }

    /**
     * Obtains the absolute path to the imported file.
     */
    Path importedFilePath() {
        Path filePath = sourceDirectory().resolve(fileRef().value());
        return filePath.normalize();
    }

    /**
     * Obtains the path of the directory with the file containing this import.
     */
    private Path sourceDirectory() {
        return file.directory();
    }
}
