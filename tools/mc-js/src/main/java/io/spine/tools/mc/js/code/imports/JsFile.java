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
import com.google.common.collect.ImmutableList;
import io.spine.code.fs.AbstractSourceFile;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.js.fs.Directory;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A JavaScript file present on a file system.
 */
public final class JsFile extends AbstractSourceFile {

    @VisibleForTesting
    public static final String EXTENSION = ".js";

    /**
     * Creates a new instance.
     *
     * @param path
     *         the path to existing JavaScript file
     */
    public JsFile(Path path) {
        super(path);
        String fileName = path.toString();
        checkArgument(fileName.endsWith(EXTENSION),
                      "A JavaScript file is expected. Passed: `%s`.", fileName);
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
     */
    public void resolveImports(Directory generatedRoot, Set<ExternalModule> modules) {
        relativizeStandardProtoImports(generatedRoot);
        resolveRelativeImports(modules);
    }

    private void relativizeStandardProtoImports(Directory generatedRoot) {
        processImports(ImportStatement::containsGoogleProtobufType,
                       statement -> statement.relativizeStandardProtoImport(generatedRoot));
    }

    private void resolveRelativeImports(Set<ExternalModule> modules) {
        processImports(ImportStatement::isUnresolvedRelativeImport,
                       statement -> statement.resolveRelativeTo(modules));
    }

    /**
     * Processes import statements in this file.
     *
     * <p>Rewrites the file using the updated imports.
     *
     * @param importFilter
     *         the predicate to filter out imports to be processed
     * @param fn
     *         the function processing an import
     */
    @VisibleForTesting
    void processImports(Predicate<ImportStatement> importFilter, ProcessImport fn) {
        load();
        ImmutableList.Builder<String> newLines = ImmutableList.builder();
        for (String line : lines()) {
            if (ImportStatement.declaredIn(line)) {
                ImportStatement importStatement = new ImportStatement(this, line);
                if (importFilter.test(importStatement)) {
                    String updated = fn.apply(importStatement).text();
                    newLines.add(updated);
                    continue;
                }
            }
            newLines.add(line);
        }
        update(newLines.build());
        store();
    }
}
