/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.io.CharStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A resource file in the classpath.
 *
 * @see #file(String, ClassLoader)
 * @see ResourceDirectory#get(String, ClassLoader)
 */
public final class Resource extends ResourceObject {

    private Resource(String path, ClassLoader classLoader) {
        super(path, classLoader);
    }

    /**
     * Creates a new reference to a resource at the context of the given class loader.
     *
     * @param path
     *         the path to the resource file
     * @param classLoader
     *         the class loader relative to which the resource is referenced
     */
    public static Resource file(String path, ClassLoader classLoader) {
        checkNotNull(path);
        checkNotNull(classLoader);
        checkNotEmptyOrBlank(path);
        return new Resource(path, classLoader);
    }

    /**
     * Obtains all the resource files by this path.
     *
     * <p>The order in which the URLs are obtained is not defined.
     *
     * <p>If there are no such files, throws an {@code IllegalStateException}.
     *
     * @return the URLs to the resolved resource files
     */
    public ImmutableList<URL> locateAll() {
        var resources = resourceEnumeration();
        var iterator = Iterators.forEnumeration(resources);
        var result = ImmutableList.copyOf(iterator);
        if (result.isEmpty()) {
            throw cannotFind();
        }
        return result;
    }

    private Enumeration<URL> resourceEnumeration() {
        try {
            var resources = resources();
            return resources;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Obtains a new {@link InputStream} to the resource.
     *
     * <p>The caller is responsible for closing the stream and handling I/O errors.
     *
     * <p>Throws an {@code IllegalStateException} if the resource cannot be resolved.
     *
     * @return new {@link InputStream}
     */
    public InputStream open() {
        var resource = locate();
        try {
            return resource.openStream();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Reads this resource as text.
     *
     * <p>Behaves similarly to {@link #open()} but works with a character stream,
     * not with a byte stream.
     */
    private Reader openAsText(Charset charset) {
        return new InputStreamReader(open(), charset);
    }

    /**
     * Reads this resource as UTF-8 text.
     *
     * <p>Behaves similarly to {@link #open()} but works with a character stream,
     * not with a byte stream.
     *
     * @see #openAsText(Charset)
     */
    public Reader openAsText() {
        return openAsText(UTF_8);
    }

    /**
     * Loads the whole resource file as a UTF-8 text file.
     *
     * @return the content of the resource file
     * @throws IllegalStateException
     *         on a failure of opening the file, e.g. if the file does not exist
     * @throws IOException
     *         on a failure of reading or closing the file
     */
    public String read() throws IOException {
        try (Reader reader = new BufferedReader(openAsText())) {
            return CharStreams.toString(reader);
        }
    }

    @Override
    public int hashCode() {
        return path().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Resource) && super.equals(o);
    }
}
