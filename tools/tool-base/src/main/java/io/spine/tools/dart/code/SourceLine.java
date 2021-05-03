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

package io.spine.tools.dart.code;

import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import io.spine.code.fs.FileReference;
import io.spine.logging.Logging;
import io.spine.tools.ExternalModule;
import org.checkerframework.checker.regex.qual.Regex;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

/**
 * Resolves import statements in a Dart file.
 */
final class SourceLine implements Logging {

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Regex(2)
    private static final Pattern IMPORT_PATTERN = compile("import [\"']([^:]+)[\"'] as (.+);");

    private final String line;
    private final SourceFile file;
    private final Matcher matcher;

    SourceLine(SourceFile file, String line) {
        this.line = line;
        this.file = file;
        this.matcher = IMPORT_PATTERN.matcher(line);
    }

    String resolveImport(Path libPath, ImmutableList<ExternalModule> modules) {
        if (!matcher.matches()) {
            return line;
        }
        Path relativeImport = toRelativeImport(libPath);
        FileReference reference = FileReference.of(relativeImport.toString());
        for (ExternalModule module : modules) {
            if (module.provides(reference)) {
                return resolveImport(relativeImport, module);
            }
        }
        return line;
    }

    private Path toRelativeImport(Path libPath) {
        FluentLogger.Api debug = _debug();
        debug.log("Import found: `%s`.", line);
        String path = matcher.group(1);
        Path absolutePath = file.path()
                                .getParent()
                                .resolve(path)
                                .normalize();
        debug.log("Resolved against this file: `%s`.", absolutePath);
        Path relativeImport = libPath.relativize(absolutePath);
        debug.log("Relative: `%s`.", relativeImport);
        return relativeImport;
    }

    private String resolveImport(Path relativeImport, ExternalModule module) {
        String importStatement = format("import 'package:%s/%s' as %s;",
                                        module.name(),
                                        relativeImport,
                                        matcher.group(2));
        log.atFine().log("Replacing with `%s`.", importStatement);
        return importStatement;
    }
}
