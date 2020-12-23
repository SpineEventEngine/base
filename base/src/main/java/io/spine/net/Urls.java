/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.net;

import io.spine.net.string.NetStringifiers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@link Url}.
 *
 * <p>Provides conversion and validation operations.
 */
public final class Urls {

    /** Prevent instantiation of this utility class. */
    private Urls() {
    }

    /**
     * Creates a new instance with the passed spec.
     *
     * <p>It is assumed that the passed value is a valid URL. No special checking is performed.
     *
     * @param spec a valid, non-null and not-empty URL spec.
     * @return new instance
     */
    public static Url create(String spec) {
        checkNotNull(spec);
        checkArgument(spec.length() > 0);
        Url result = NetStringifiers.forUrl()
                                    .reverse()
                                    .convert(spec);
        return result;
    }

    /**
     * Performs String conversion for passed value.
     *
     * @param url a value to convert to string
     * @return String representation of the given URL
     */
    public static String toString(Url url) {
        checkNotNull(url);
        String result = NetStringifiers.forUrl()
                                       .convert(url);
        return result;
    }
}
