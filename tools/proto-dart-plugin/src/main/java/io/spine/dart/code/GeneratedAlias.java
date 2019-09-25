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

package io.spine.dart.code;

import com.google.common.base.Splitter;

import java.util.List;

import static com.google.common.base.CharMatcher.anyOf;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An alias for an imported Dart library.
 *
 * <p>Consists of the imported file path concatenated with {@code _} (underscore) symbols.
 */
public final class GeneratedAlias extends Reference {

    private static final Splitter pathSplitter = Splitter.on(anyOf("/\\"));
    private static final char ESCAPE_DELIMITER = '_';

    public GeneratedAlias(String pathToFile) {
        super(escapePathToAlias(pathToFile));
    }

    private static String escapePathToAlias(String path) {
        checkNotNull(path);
        List<String> pathElements = pathSplitter.splitToList(path);
        StringBuilder alias = new StringBuilder(path.length() + 1);
        for (String element : pathElements) {
            alias.append(ESCAPE_DELIMITER)
                 .append(element);
        }
        return alias.toString();
    }
}
