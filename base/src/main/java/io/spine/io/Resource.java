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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

public final class Resource {

    private final String path;

    private Resource(String path) {
        this.path = path;
    }

    public static Resource file(String path) {
        checkNotEmptyOrBlank(path);
        return new Resource(path);
    }

    public URL locate() {
        URL resource = classLoader().getResource(path);
        checkFound(resource);
        return resource;
    }

    public Iterable<URL> locateAll() {
        Enumeration<URL> resources = getResourceEnumeration();
        UnmodifiableIterator<URL> iterator = Iterators.forEnumeration(resources);
        ImmutableList<URL> result = ImmutableList.copyOf(iterator);
        checkState(!result.isEmpty(),
                   "Could not find any resources by path `%s` in classpath.",
                   path);
        return result;
    }

    private Enumeration<URL> getResourceEnumeration() {
        try {
            Enumeration<URL> resources = classLoader().getResources(path);
            return resources;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    public InputStream open() {
        InputStream stream = classLoader().getResourceAsStream(path);
        checkFound(stream);
        return stream;
    }

    private void checkFound(@Nullable Object resourceHandle) {
        checkState(resourceHandle != null, "Could not find resource `%s` in classpath.", path);
    }

    private static ClassLoader classLoader() {
        return Thread.currentThread()
                     .getContextClassLoader();
    }
}
