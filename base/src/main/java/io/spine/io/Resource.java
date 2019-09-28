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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A resource file in the classpath.
 *
 * <p>The {@code Thread.currentThread().getContextClassLoader()} is used to load the resource from
 * the classpath.
 */
public final class Resource {

    private final String path;

    private Resource(String path) {
        this.path = path;
    }

    /**
     * Creates a new instance.
     *
     * @param path
     *         the path to the resource file, relative to the classpath.
     */
    public static Resource file(String path) {
        checkNotEmptyOrBlank(path);
        return new Resource(path);
    }

    /**
     * Checks if the resource with such a name exists in the classpath.
     *
     * @return {@code true} if the resource is present, {@code false} otherwise
     */
    public boolean exists() {
        URL resource = classLoader().getResource(path);
        return resource != null;
    }

    /**
     * Obtains a {@link URL} of the resolved resource.
     *
     * <p>If the resource cannot be resolved (i.e. the file does not exist), throws
     * an {@code IllegalStateException}.
     *
     * @return the resource URL
     */
    public URL locate() {
        URL resource = classLoader().getResource(path);
        checkFound(resource);
        return resource;
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
        Enumeration<URL> resources = resourceEnumeration();
        UnmodifiableIterator<URL> iterator = Iterators.forEnumeration(resources);
        ImmutableList<URL> result = ImmutableList.copyOf(iterator);
        checkState(!result.isEmpty(),
                   "Could not find any resources by path `%s` in classpath.",
                   path);
        return result;
    }

    private Enumeration<URL> resourceEnumeration() {
        try {
            Enumeration<URL> resources = classLoader().getResources(path);
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
        InputStream stream = classLoader().getResourceAsStream(path);
        checkFound(stream);
        return stream;
    }

    /**
     * Reads this resource as text.
     *
     * <p>Behaves similarly to {@link #open()} but works with a character stream, not with a byte
     * stream.
     */
    public Reader openAsText(Charset charset) {
        return new InputStreamReader(open(), charset);
    }

    /**
     * Reads this resource as UTF-8 text.
     *
     * <p>Behaves similarly to {@link #open()} but works with a character stream, not with a byte
     * stream.
     *
     * @see #openAsText(Charset)
     */
    public Reader openAsText() {
        return openAsText(UTF_8);
    }

    private void checkFound(@Nullable Object resourceHandle) {
        checkState(resourceHandle != null, "Could not find resource `%s` in classpath.", path);
    }

    private static ClassLoader classLoader() {
        return Thread.currentThread()
                     .getContextClassLoader();
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }
        Resource resource = (Resource) o;
        return Objects.equal(path, resource.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
