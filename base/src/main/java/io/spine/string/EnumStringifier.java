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

package io.spine.string;

import io.spine.annotation.VisibleForTesting;

import java.io.Serial;

import static java.lang.String.format;

/**
 * A stringifier for {@code enum} values.
 *
 * @param <E>
 *         the type of the {@code enum}
 */
final class EnumStringifier<E extends Enum<E>> extends SerializableStringifier<E> {

    @Serial
    private static final long serialVersionUID = 0L;

    private final Class<E> enumClass;

    EnumStringifier(Class<E> enumClass) {
        super(identity(enumClass));
        this.enumClass = enumClass;
    }

    @Override
    protected String toString(E e) {
        return e.toString();
    }

    @Override
    protected E fromString(String s) {
        var result = Enum.valueOf(enumClass, s);
        return result;
    }

    @VisibleForTesting
    static <E extends Enum<E>> String identity(Class<E> enumClass) {
        return format("Stringifiers.newForEnum(%s.class)", enumClass.getSimpleName());
    }
}
