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

import com.google.common.collect.Iterators;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A utility for loading resource files from classpath.
 */
public final class ResourceFiles {

    /**
     * Prevents the utility class instantiation.
     */
    private ResourceFiles() {
    }

    /**
     * Loads all the resource files with the given path.
     *
     * @param filePath the path to the resource file to load
     * @return an {@code Iterator} of {@link URL}s; each of the URLs represents a single resource
     *         file
     * @throws IllegalStateException upon an I/O error
     * @see ClassLoader#getResources(String)
     */
    public static Iterator<URL> loadAll(String filePath) {
        try {
            return tryLoadAll(filePath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Loads all the resource files with the given path or throws an {@link IOException} if
     * an I/O error occurs.
     *
     * <p>Use {@link #loadAll(String)} in order to ignore any {@code IOException}.
     *
     * @param filePath the path to the resource file to load
     * @return an {@code Iterator} of {@link URL}s; each of the URLs represents a single resource
     *         file
     * @throws IOException upon an I/O error
     * @see ClassLoader#getResources(String)
     * @see #loadAll(String)
     */
    static Iterator<URL> tryLoadAll(String filePath) throws IOException {
        checkNotEmptyOrBlank(filePath);
        ClassLoader contextClassLoader = Thread.currentThread()
                                               .getContextClassLoader();
        Enumeration<URL> resources = contextClassLoader.getResources(filePath);
        Iterator<URL> result = Iterators.forEnumeration(resources);
        return result;
    }
}
