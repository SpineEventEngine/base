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

package io.spine.tools.dart.fs;

import com.google.common.flogger.FluentLogger;
import io.spine.logging.Logging;
import io.spine.tools.code.Element;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.fs.FileReference;
import org.checkerframework.checker.regex.qual.Regex;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

/**
 * A source code line with an import statement.
 */
final class ImportStatement implements Element, Logging {

    @Regex(2)
    private static final Pattern PATTERN = compile("import [\"']([^:]+)[\"'] as (.+);");

    private final DartFile file;
    private final String text;
    private final Matcher matcher;

    /**
     * Creates a new instance with the passed value.
     *
     * @param file
     *         the file declaring the line
     * @param text
     *         the text of the source code line with the import statement
     */
    ImportStatement(DartFile file, String text) {
        this.text = checkNotNull(text);
        this.file = checkNotNull(file);
        Matcher matcher = PATTERN.matcher(text);
        checkArgument(
                matcher.matches(),
                "The passed text is not recognized as an import statement (`%s`).", text
        );
        this.matcher = matcher;
    }

    /**
     * Tells if the passed text is an import statement.
     */
    static boolean isDeclaredIn(String text) {
        checkNotNull(text);
        Matcher matcher = PATTERN.matcher(text);
        return matcher.matches();
    }

    /**
     * If this line contains an import statement, converts it to a relative import statement
     * wherever possible.
     *
     * <p>If this line is not an import statement, returns it as is.
     *
     * <p>For import statements, converts an absolute path found after
     * {@code import} clause into a relative path, if one of the passed modules contains
     * such a file.
     *
     * <p>If none of the modules have such a file, returns the source code line as is.
     *
     * @param libPath
     *         the path to the {@code lib} project folder
     * @param modules
     *         the modules of the project to check the file referenced in
     *         the {@code import} statement
     */
    public ImportStatement resolve(Path libPath, ExternalModules modules) {
        Path relativePath = importRelativeTo(libPath);
        FileReference reference = FileReference.of(relativePath);
        for (ExternalModule module : modules.asList()) {
            if (module.provides(reference)) {
                return resolve(module, relativePath);
            }
        }
        return this;
    }

    /**
     * Transforms the path found in the import statement to a path relative
     * to the file of this source line.
     */
    private Path importRelativeTo(Path libPath) {
        FluentLogger.Api debug = _debug();
        debug.log("Import statement found in line: `%s`.", text);
        String path = matcher.group(1);
        Path absolutePath =
                file.path()
                      .getParent()
                      .resolve(path)
                      .normalize();
        debug.log("Resolved against this file: `%s`.", absolutePath);
        Path relativePath = libPath.relativize(absolutePath);
        debug.log("Relative path: `%s`.", relativePath);
        return relativePath;
    }

    private ImportStatement resolve(ExternalModule module, Path relativePath) {
        String resolved = format(
                "import 'package:%s/%s' as %s;",
                module.name(), relativePath, matcher.group(2)
        );
        _debug().log("Replacing with `%s`.", resolved);
        return new ImportStatement(file, resolved);
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportStatement)) {
            return false;
        }
        ImportStatement other = (ImportStatement) o;
        return text.equals(other.text) && file.equals(other.file);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
