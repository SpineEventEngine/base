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

package io.spine.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.lineSeparator;

/**
 * Utilities for working with text lines separated by {@link System#lineSeparator()}.
 */
public final class Lines {

    /**
     * Prevents instantiation of this utility class.
     */
    private Lines() {
    }

    /**
     * Splits the passed text separated with {@link System#lineSeparator()} into lines.
     *
     * @return unmodifiable list of code lines
     */
    public static List<String> split(String text) {
        checkNotNull(text);
        return splitter().splitToList(text);
    }

    /**
     * Join the lines separated with {@link System#lineSeparator()}.
     */
    public static String join(Iterable<String> lines) {
        checkNotNull(lines);
        return joiner().join(lines);
    }

    /**
     * Join the lines separating them with {@link System#lineSeparator()}.
     */
    public static String join(String[] lines) {
        checkNotNull(lines);
        return joiner().join(lines);
    }

    /**
     * Obtains the {@link Joiner} on {@link System#lineSeparator()}.
     */
    public static Joiner joiner() {
        return Joiner.on(lineSeparator());
    }

    /**
     * Obtains the {@link Splitter} on {@link System#lineSeparator()}.
     */
    public static Splitter splitter() {
        return Splitter.on(lineSeparator());
    }
}
