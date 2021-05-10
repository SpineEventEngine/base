/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.fs;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.value.StringTypeValue;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A reference to a directory.
 *
 * <p>May include parent directories separated by {@linkplain FileReference#separator() slashes},
 * e.g. {@code root/sub}.
 */
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden") // provided by the parent class.
@Immutable
public final class DirectoryReference
        extends StringTypeValue
        implements Comparable<DirectoryReference> {

    private static final long serialVersionUID = 0L;
    private static final DirectoryReference CURRENT = new DirectoryReference("");

    private DirectoryReference(String value) {
        super(value);
    }

    /**
     * Creates a new instance.
     *
     * @param value
     *         the value of the reference
     * @return a new instance
     */
    public static DirectoryReference of(String value) {
        checkNotEmptyOrBlank(value);
        return new DirectoryReference(value);
    }

    /**
     * Obtains the reference to the current directory.
     */
    public static DirectoryReference currentDir() {
        return CURRENT;
    }

    /**
     * Obtains all directory names composing this reference.
     */
    public ImmutableList<String> elements() {
        Iterable<String> elements = FileReference.splitter().split(value());
        return ImmutableList.copyOf(elements);
    }

    @Override
    public int compareTo(DirectoryReference o) {
        return value().compareTo(o.value());
    }
}
