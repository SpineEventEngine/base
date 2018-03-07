/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.util;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.IoUtil.close;
import static java.lang.String.format;

/**
 * Utilities for working with property files.
 *
 * @author Alexander Litus
 * @author Alexander Yevsyukov
 */
public class PropertyFiles {

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

        final ImmutableSet.Builder<Properties> result = ImmutableSet.builder();
        final Enumeration<URL> resources = getResources(propsFilePath);
        if (resources == null) {
            return result.build();
        }
        while (resources.hasMoreElements()) {
            final URL resourceUrl = resources.nextElement();
            final Properties properties = loadPropertiesFile(resourceUrl);
            result.add(properties);
        }
        return result.build();
    }

    private static Enumeration<URL> getResources(String propsFilePath) {
        final ClassLoader classLoader = getContextClassLoader();
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(propsFilePath);
        } catch (IOException e) {
            warn(e, "Failed to load resources: %s", propsFilePath);
        }
        return resources;
    }

    private static Properties loadPropertiesFile(URL resourceUrl) {
        final Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = resourceUrl.openStream();
            properties.load(inputStream);
        } catch (IOException e) {
            warn(e, "Failed to load properties file from: %s", resourceUrl);
        } finally {
            close(inputStream);
        }
        return properties;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread()
                     .getContextClassLoader();
    }

    private static void warn(Throwable e, String errorFormat, Object... params) {
        final Logger log = log();
        if (log.isWarnEnabled()) {
            final String msg = format(errorFormat, params);
            log.warn(msg, e);
        }
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
