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

package io.spine.tools.compiler.annotation;

import com.google.common.collect.ImmutableSet;
import io.spine.logging.Logging;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walkFileTree;

final class GlobPattern implements Logging {

    private static final String GLOB_FORMAT = "glob:%s";

    private final PathMatcher matcher;
    private final String rawPattern;

    private GlobPattern(PathMatcher matcher, String pattern) {
        this.matcher = matcher;
        rawPattern = pattern;
    }

    static GlobPattern compile(String pattern) {
        checkArgument(!isNullOrEmpty(pattern));
        String globPattern = format(GLOB_FORMAT, pattern);
        PathMatcher matcher = FileSystems.getDefault()
                                         .getPathMatcher(globPattern);
        return new GlobPattern(matcher, globPattern);
    }

    ImmutableSet<Path> findIn(Path directory) {
        checkArgument(exists(directory));
        checkArgument(isDirectory(directory));

        Lookup lookup = new Lookup();
        return lookup.executeIn(directory);
    }

    private final class Lookup extends SimpleFileVisitor<Path> {

        private final ImmutableSet.Builder<Path> matchedFiles = ImmutableSet.builder();

        private ImmutableSet<Path> executeIn(Path startDirectory) {
            try {
                walkFileTree(startDirectory, this);
            } catch (IOException e) {
                _error(e, "Unable to compete lookup by pattern `{}`.", rawPattern);
            }
            return matchedFiles.build();
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            checkNotNull(file);
            checkNotNull(attrs);
            if (matcher.matches(file)) {
                matchedFiles.add(file);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            _error(exc, "Unable to visit file `{}`.", file);
            return CONTINUE;
        }
    }
}
