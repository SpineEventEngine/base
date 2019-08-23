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

package io.spine.string;

import com.google.common.annotations.VisibleForTesting;

import static java.lang.String.format;

/**
 * A stringifier for {@code enum} values.
 *
 * @param <E>
 *         the type of the {@code enum}
 */
public final class EnumStringifier<E extends Enum<E>> extends SerializableStringifier<E> {

    private static final long serialVersionUID = 0L;

    private static final String DEFAULT_IDENTITY_FORMAT = "a stringifier of enum class `%s`";

    private final Class<E> enumClass;

    public EnumStringifier(String identity, Class<E> enumClass) {
        super(identity);
        this.enumClass = enumClass;
    }

    public EnumStringifier(Class<E> enumClass) {
        this(defaultIdentity(enumClass), enumClass);
    }

    @Override
    protected final String toString(E e) {
        return e.toString();
    }

    @Override
    protected final E fromString(String s) {
        E result = Enum.valueOf(enumClass, s);
        return result;
    }

    @VisibleForTesting
    static <E extends Enum<E>> String defaultIdentity(Class<E> enumClass) {
        return format(DEFAULT_IDENTITY_FORMAT, enumClass.getCanonicalName());
    }
}
