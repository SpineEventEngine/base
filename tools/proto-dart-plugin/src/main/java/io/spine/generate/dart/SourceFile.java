/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.generate.dart;

import com.google.common.collect.ImmutableList;
import io.spine.code.AbstractSourceFile;
import io.spine.code.fs.js.FileReference;
import io.spine.logging.Logging;
import io.spine.tools.code.ExternalModule;
import org.checkerframework.checker.regex.qual.Regex;
import org.gradle.api.GradleException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

final class SourceFile extends AbstractSourceFile implements Logging {

    @Regex(2)
    private static final Pattern IMPORT_PATTERN = compile("import [\"']([^:]+)[\"'] as (.+);");

    private List<String> lines;

    private SourceFile(Path path, List<String> lines) {
        super(path);
        this.lines = lines;
    }

    static SourceFile read(Path path) {
        checkNotNull(path);
        try {
            List<String> lines = readAllLines(path);
            return new SourceFile(path, lines);
        } catch (IOException e) {
            throw new GradleException(format("Unable to read file `%s`.", path), e);
        }
    }

    static boolean isGeneratedDart(Path file) {
        if (!isRegularFile(file)) {
            return false;
        }
        return file.endsWith(".pb.dart")
                || file.endsWith(".pbenum.dart")
                || file.endsWith(".pbserver.dart")
                || file.endsWith(".pbjson.dart");
    }

    void resolveImports(ImmutableList<ExternalModule> modules, Path libPath) {
        lines = lines.stream()
                     .map(line -> resolveImportInLine(line, modules, libPath))
                     .collect(toList());
    }

    private String resolveImportInLine(String line,
                                       ImmutableList<ExternalModule> modules,
                                       Path libPath) {
        Matcher matcher = IMPORT_PATTERN.matcher(line);
        if (matcher.matches()) {
            _debug().log("Import found: `%s`", line);
            String path = matcher.group(1);
            Path absolutePath = path().getParent()
                                      .resolve(path)
                                      .normalize();
            _debug().log("Resolved against this file: `%s`", absolutePath);
            Path relativeImport = libPath.relativize(absolutePath);
            _debug().log("Relative: `%s`", relativeImport);
            FileReference reference = FileReference.of(relativeImport.toString());
            for (ExternalModule module : modules) {
                if (module.provides(reference)) {
                    String importStatement = format("import 'package:%s/%s' as %s;",
                                                    module.name(),
                                                    relativeImport,
                                                    matcher.group(2));
                    _debug().log("Replacing with %s", importStatement);
                    return importStatement;
                }
            }
        }
        return line;
    }

    void store() {
        Path path = path();
        try {
            write(path, lines);
        } catch (IOException e) {
            throw new GradleException(format("Unable to write file `%s`.", path), e);
        }
    }
}
