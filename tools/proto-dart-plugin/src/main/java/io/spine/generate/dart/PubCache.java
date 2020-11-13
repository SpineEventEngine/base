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

import com.google.common.base.Strings;
import org.apache.tools.ant.taskdefs.condition.Os;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.file.Files.exists;
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS;

/**
 * A utility for working with the local Pub cache.
 */
@SuppressWarnings("WeakerAccess") // Part of the public API.
public final class PubCache {

    private static final String BIN = "bin";
    private static final String LOCAL_APP_DATA_ENV = "LOCALAPPDATA";
    private static final String PUB_CACHE_ENV = "PUB_CACHE";

    private static final Path location = findCache();

    /**
     * Prevents the utility class instantiation.
     */
    private PubCache() {
    }

    /**
     * Obtains the path to the local Pub cache directory.
     */
    private static Path findCache() {
        return customPath().orElseGet(PubCache::defaultPath);
    }

    private static Path defaultPath() {
        Path pubCache;
        if (Os.isFamily(FAMILY_WINDOWS)) {
            @SuppressWarnings("CallToSystemGetenv")
            String localAppData = System.getenv(LOCAL_APP_DATA_ENV);
            pubCache = Paths.get(localAppData).resolve("Pub").resolve("Cache").resolve(BIN);
        } else {
            @SuppressWarnings("AccessOfSystemProperties")
            String userHome = System.getProperty("user.home");
            pubCache = Paths.get(userHome).resolve(".pub-cache").resolve(BIN);
        }
        return pubCache;
    }

    private static Optional<Path> customPath() {
        @SuppressWarnings("CallToSystemGetenv")
        String customPubCache = System.getenv(PUB_CACHE_ENV);
        if (Strings.isNullOrEmpty(customPubCache)) {
            return Optional.empty();
        }
        Path pubCachePath = Paths.get(customPubCache);
        if (!exists(pubCachePath)) {
            return Optional.empty();
        }
        Path resolvedBin = pubCachePath.resolve(BIN);
        if (exists(resolvedBin)) {
            return Optional.of(resolvedBin);
        }
        return Optional.empty();
    }

    /**
     * Obtains the path to the local Pub cache {@code bin} directory.
     *
     * <p>{@code bin} is where all the globally installed executable artifacts end up.
     */
    public static Path bin() {
        return location;
    }
}
