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

import com.google.common.base.MoreObjects;
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
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;

/**
 * A resource file in the classpath.
 *
 * <p>By default the resource will be loaded using the class loader of this class.
 * In order to use another loader, please use {@link #file(String, ClassLoader)}.
 * 
 * @see #file(String)
 */
public final class Resource {

    private final String path;
    private final @Nullable ClassLoader customLoader;

    private Resource(String path, @Nullable ClassLoader customLoader) {
        this.path = path;
        this.customLoader = customLoader;
    }

    /**
     * Creates a new resource reference.
     *
     * @param path
     *         the path to the resource file, relative to the classpath
     */
    public static Resource file(String path) {
        checkNotEmptyOrBlank(path);
        return new Resource(path, null);
    }

    /**
     * Creates a new reference to a resource at the context of the given class.
     *
     * @param path
     *         the path to the resource file, relative to {@code contextClass}
     * @param customLoader
     *         the class relative to which the resource is referenced
     */
    public static Resource file(String path, ClassLoader customLoader) {
        checkNotNull(customLoader);
        checkNotEmptyOrBlank(path);
        return new Resource(path, customLoader);
    }

    private boolean loaderCustomized() {
        return customLoader != null;
    }

    private @Nullable URL findUrl() {
        URL url = classLoader().getResource(path);
        return url;
    }

    private ClassLoader classLoader() {
        return MoreObjects.firstNonNull(customLoader, Resource.class.getClassLoader());
    }

    private String classLoaderDiag() {
        ClassLoader loader = classLoader();
        String fmt = loaderCustomized()
                     ? "the assigned class loader `%s`."
                     : "the class loader `%s`.";
        return format(fmt, loader);
    }

    /**
     * Checks if the resource with such a name exists in the classpath.
     *
     * @return {@code true} if the resource is present, {@code false} otherwise
     */
    public boolean exists() {
        URL resource = findUrl();
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
        URL url = findUrl();
        if (url == null) {
            throw newIllegalStateException(
                    "Unable to find the resource `%s` using `%s`.",
                    path, classLoaderDiag()
            );
        }
        return url;
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
        checkState(!result.isEmpty(), "Could not find any resources `%s` using `%s`.",
                   path, classLoaderDiag());
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
        URL resource = locate();
        try {
            return resource.openStream();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Reads this resource as text.
     *
     * <p>Behaves similarly to {@link #open()} but works with a character stream, not with a byte
     * stream.
     */
    private Reader openAsText(Charset charset) {
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

    @Override
    public String toString() {
        return loaderCustomized() ? path + " via " + classLoader() : path;
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
