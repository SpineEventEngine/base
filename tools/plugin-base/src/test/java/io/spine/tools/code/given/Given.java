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

package io.spine.tools.code.given;

import io.spine.code.fs.js.Directory;
import io.spine.tools.code.DirectoryPattern;
import io.spine.tools.code.ExternalModule;
import io.spine.tools.code.ImportStatement;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class Given {

    /** Prevents instantiation of this utility class. */
    private Given() {
    }

    public static ImportStatement importWithPath(String path, File importOrigin) {
        String importText = format("let foo = require('%s');", path);
        return new ImportStatement(importText, importOrigin);
    }

    public static String relativeImportPath() {
        return "../path-relative-to-parent.js";
    }

    private static Directory protoRoot(String sourceSetName) {
        Path path = Paths.get("src")
                         .resolve(sourceSetName)
                         .resolve("proto");
        return Directory.at(path.toAbsolutePath());
    }

    public static ExternalModule newModule(String moduleName, String directoryPattern) {
        DirectoryPattern pattern = DirectoryPattern.of(directoryPattern);
        List<DirectoryPattern> patterns = singletonList(pattern);
        return new ExternalModule(moduleName, patterns);
    }
}
