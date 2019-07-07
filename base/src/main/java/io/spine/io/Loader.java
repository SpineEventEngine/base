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

package io.spine.io;

import com.google.common.collect.ImmutableSet;
import io.spine.logging.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * Performs loading of multiple property files from the specified path.
 */
final class Loader implements Logging {

    private static final Loader INSTANCE = new Loader();

    static Loader instance() {
        return INSTANCE;
    }

    ImmutableSet<Properties> loadAllProperties(Resource resource) {
        checkNotNull(resource);
        if (resource.exists()) {
            ImmutableSet<Properties> resources =
                    resource.locateAll()
                            .stream()
                            .map(this::loadPropertiesFile)
                            .collect(toImmutableSet());
            return resources;
        } else {
            _error().log("Failed to load resource: {}", resource);
            return ImmutableSet.of();
        }
    }

    Properties loadPropertiesFile(URL resourceUrl) {
        Properties properties = new Properties();
        try (InputStream inputStream = resourceUrl.openStream()) {
            properties.load(inputStream);
        } catch (IOException e) {
            _error().withCause(e)
                    .log("Failed to load properties file from: %s", resourceUrl);
        }
        return properties;
    }
}
