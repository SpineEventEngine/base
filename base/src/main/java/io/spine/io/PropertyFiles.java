/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.logging.Logging.warn;

/**
 * Utilities for working with property files.
 *
 * @author Alexander Litus
 * @author Alexander Yevsyukov
 * @author Dmytro Dashenkov
 */
public final class PropertyFiles {

    /** Prevents instantiation of this utility class. */
    private PropertyFiles() {
    }

    /**
     * Loads property file(s) at the passed path into memory.
     *
     * <p>Logs {@link IOException} if it occurs.
     *
     * @param propsFilePath the path of the {@code .properties} file to load
     * @return set with loaded data
     */
    public static Set<Properties> loadAllProperties(String propsFilePath) {
        checkNotNull(propsFilePath);
        try {
            return doLoad(propsFilePath);
        } catch (IOException e) {
            warn(log(), e, "Failed to load resources: %s", propsFilePath);
            return ImmutableSet.of();
        }
    }

    private static Set<Properties> doLoad(String filePath)
            throws IOException {
        Iterator<URL> resources = ResourceFiles.tryLoadAll(filePath);
        ImmutableSet.Builder<Properties> result = ImmutableSet.builder();
        while (resources.hasNext()) {
            URL resourceUrl = resources.next();
            Properties properties = loadPropertiesFile(resourceUrl);
            result.add(properties);
        }
        return result.build();
    }

    private static Properties loadPropertiesFile(URL resourceUrl) {
        Properties properties = new Properties();
        try (InputStream inputStream = resourceUrl.openStream()) {
            properties.load(inputStream);
        } catch (IOException e) {
            warn(log(), e, "Failed to load properties file from: %s", resourceUrl);
        }
        return properties;
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(PropertyFiles.class);
    }
}
