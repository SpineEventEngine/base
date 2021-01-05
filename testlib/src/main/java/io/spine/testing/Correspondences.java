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

package io.spine.testing;

import com.google.common.truth.Correspondence;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A factory of {@link Correspondence}s for constructing fluent assertions for collection elements.
 */
public final class Correspondences {

    /**
     * Prevents the utility class instantiation.
     */
    private Correspondences() {
    }

    /**
     * Obtains a {@link Correspondence} of an object to its type.
     *
     * <p>Elements of a collection can be matched to their class using this correspondence.
     *
     * <p>Example:
     * {@code
     * assertThat(objects)
     *     .comparingElementsUsing(type())
     *     .containsExactly(String.class, String.class);
     * }
     *
     * @param <T>
     *         type of the input object
     * @return correspondence by type
     */
    @SuppressWarnings("NullableProblems") // False positive.
    public static <T> Correspondence<T, @NonNull Class<?>> type() {
        return Correspondence.from(
                (o, cls) -> cls.isInstance(o), "is an instance of"
        );
    }
}
