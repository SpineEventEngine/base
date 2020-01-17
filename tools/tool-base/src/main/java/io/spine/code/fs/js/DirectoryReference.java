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

package io.spine.code.fs.js;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.spine.value.StringTypeValue;

import java.util.List;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A reference to a directory.
 *
 * <p>May include parent directories separated by {@linkplain FileReference#separator() slashes},
 * e.g. {@code root/sub}.
 */
public final class DirectoryReference extends StringTypeValue {

    private static final long serialVersionUID = 0L;

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
     * Obtains all directory names composing this reference.
     */
    public List<String> elements() {
        Iterable<String> elements = Splitter.on(FileReference.separator())
                                            .split(value());
        return ImmutableList.copyOf(elements);
    }
}
