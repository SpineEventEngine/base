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

package io.spine.dart.generate;

import com.google.common.base.Strings;
import org.apache.tools.ant.taskdefs.condition.Os;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.Files.exists;
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS;

/**
 * Locates the Dart {@code protoc} plugin executable in the local Pub cache.
 *
 * <p>See <a href="https://dart.dev/tools/pub/cmd/pub-global#running-a-script-from-your-path">Pub
 * documentation</a>.
 */
final class CachedDartProtocPlugin {

    private static final boolean WINDOWS = Os.isFamily(FAMILY_WINDOWS);
    private static final String SCRIPT_EXTENSION = WINDOWS ? ".bat" : "";
    private static final String SCRIPT_FILE_NAME = "protoc-gen-dart" + SCRIPT_EXTENSION;
    private static final String BIN = "bin";
    private static final String APP_DATA_ENV = "APPDATA";
    private static final String PUB_CACHE_ENV = "PUB_CACHE";

    private static final String DOC_LINK =
            "https://github.com/dart-lang/protobuf/tree/master/protoc_plugin";

    private static Path resolved = null;

    /**
     * Prevents the utility class instantiation.
     */
    private CachedDartProtocPlugin() {
    }

    static synchronized Path locate() {
        if (resolved == null) {
            Path pathToExecutable = inCustomPubCache()
                    .orElseGet(CachedDartProtocPlugin::inDefaultPubCache);
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

    /**
     * Looks up the plugin executable in the custom pub cache.
     *
     * <p>A custom pub cache is a path defined in the {@code PUB_CACHE} environmental variable.
     *
     * @return path to the plugin executable or {@code Optional.empty()} if a custom cache is not
     *         defined or the file does not exist
     */
    private static Optional<Path> inCustomPubCache() {
        @SuppressWarnings("CallToSystemGetenv")
        String customPubCache = System.getenv(PUB_CACHE_ENV);
        if (Strings.isNullOrEmpty(customPubCache)) {
            return Optional.empty();
        }
        Path pubCachePath = Paths.get(customPubCache);
        if (!exists(pubCachePath)) {
            return Optional.empty();
        }
        Path resolved = pubCachePath.resolve(SCRIPT_FILE_NAME);
        if (exists(resolved)) {
            return Optional.of(resolved);
        }
        Path resolvedInBin = pubCachePath.resolve(BIN)
                                         .resolve(SCRIPT_FILE_NAME);
        if (exists(resolvedInBin)) {
            return Optional.of(resolvedInBin);
        }
        return Optional.empty();
    }

    /**
     * Locates the plugin executable in the platform-specific default location.
     *
     * @return a path to the executable which may or may not exist
     */
    private static Path inDefaultPubCache() {
        return WINDOWS ? defaultForWindows() : defaultForNonWindows();
    }

    /**
     * Returns the path {@code %APPDATA%/Pub/Cache/bin/protoc-gen-dart.bat}.
     */
    private static Path defaultForWindows() {
        @SuppressWarnings("CallToSystemGetenv")
        String appDataDir = System.getenv(APP_DATA_ENV);
        return Paths.get(appDataDir, "Pub", "Cache", BIN, SCRIPT_FILE_NAME);
    }

    /**
     * Returns the path {@code $HOME/.pub-cache/bin/protoc-gen-dart}.
     */
    private static Path defaultForNonWindows() {
        @SuppressWarnings("AccessOfSystemProperties")
        String homeDir = System.getProperty("user.home");
        return Paths.get(homeDir, ".pub-cache", BIN, SCRIPT_FILE_NAME);
    }
}
