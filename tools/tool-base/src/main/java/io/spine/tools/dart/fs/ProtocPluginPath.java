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

import io.spine.tools.OsFamily;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.Files.exists;

/**
 * Locates the Dart {@code protoc} plugin executable in the local Pub cache.
 *
 * @see <a href="https://dart.dev/tools/pub/cmd/pub-global#running-a-script-from-your-path">
 * Pub documentation</a>.
 */
public final class ProtocPluginPath {

    private static final boolean WINDOWS = OsFamily.Windows.isCurrent();
    private static final String SCRIPT_EXTENSION = WINDOWS ? ".bat" : "";
    private static final String SCRIPT_FILE_NAME = "protoc-gen-dart" + SCRIPT_EXTENSION;

    private static final String DOC_LINK =
            "https://github.com/dart-lang/protobuf/tree/master/protoc_plugin";

    private static Path resolved = null;

    /** Prevents the utility class instantiation. */
    private ProtocPluginPath() {
    }

    /**
     * Obtains the path to the plugin.
     */
    public static synchronized Path locate() {
        if (resolved == null) {
            Path pathToExecutable = PubCache.bin().resolve(SCRIPT_FILE_NAME);
            checkState(exists(pathToExecutable),
                       "Protoc plugin for Dart code generation is not installed. " +
                               "`protoc_plugin` should be activated globally. " +
                               "See %s for installation guide.",
                       DOC_LINK);
            resolved = pathToExecutable.toAbsolutePath()
                                       .normalize();
        }
        return resolved;
    }
}
