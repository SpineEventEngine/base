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
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.System.lineSeparator;

/**
 * A text with lines separated by {@link System#lineSeparator()}.
 *
 * @apiNote Even though this class provides static utilities for splitting and merging
 *         lines, please prefer using instance methods of this class.
 * @deprecated Please use a similar type for
 *      the <a href="https://github.com/SpineEventEngine/text">Text library</a>.
 */
@Immutable
@Deprecated
public final class Text implements Iterable<String> {

    private static final Splitter SPLITTER = Splitter.on(lineSeparator());
    private static final Joiner JOINER = Joiner.on(lineSeparator());

    private final ImmutableList<String> lines;
    private final String value;

    /**
     * Creates a new instance with the given lines.
     *
     * <p>All the given lines must not contain a {@linkplain System#lineSeparator()
     * line separator}.
     *
     * @throws IllegalArgumentException
     *          if any of the lines contains the {@linkplain System#lineSeparator()
     *          line separator}
     */
    public Text(Iterable<String> lines) {
        checkNotNull(lines);
        checkNoSeparators(lines);
        this.lines = ImmutableList.copyOf(lines);
        this.value = join(lines);
    }

    private static void checkNoSeparators(Iterable<String> lines) {
        lines.forEach(l -> {
            if (containsSeparator(l)) {
                throw newIllegalArgumentException("The line contains line separator: `%s`.", l);
            }
        });
    }

    /**
     * Creates a new instance splitting the given text into lines.
     */
    public Text(String text) {
        this(split(text));
    }

    /**
     * Creates a new instance with the given lines.
     *
     * <p>All the given lines must not contain a {@linkplain System#lineSeparator()
     * line separator}.
     *
     * @throws IllegalArgumentException
     *          if any of the lines contains the {@linkplain System#lineSeparator()
     *          line separator}
     */
    public Text(String[] lines) {
        this(ImmutableList.copyOf(lines));
    }

    /**
     * Creates a new list with the given lines.
     *
     * <p>All the given lines must not contain a {@linkplain System#lineSeparator()
     * line separator}.
     *
     * @throws IllegalArgumentException
     *          if any of the lines contains the {@linkplain System#lineSeparator()
     *          line separator}
     */
    public static Text of(String... lines) {
        checkNotNull(lines);
        return new Text(lines);
    }

    /**
     * Obtains a read-only view of the text lines.
     */
    public List<String> lines() {
        return lines;
    }

    /**
     * Obtains the text as joined lines.
     *
     * <p>This method always returns same object, so repeated calls have no performance effect.
     *
     * @see #toString()
     */
    public String value() {
        return value;
    }

    /**
     * Tells if the given string is contained by any of the text lines.
     *
     * @param s
     *        the string to find. Must not contain a {@linkplain System#lineSeparator()
     *        line separator}.
     * @return {@code true} if at least one line contains the given string, {@code false} otherwise
     * @throws IllegalArgumentException
     *          if the given string contains the {@linkplain System#lineSeparator()
     *          line separator}
     */
    public boolean contains(String s) {
        checkArgument(!containsSeparator(s));
        var result = lines.stream().anyMatch(line -> line.contains(s));
        return result;
    }

    private static boolean containsSeparator(String s) {
        return s.contains(lineSeparator());
    }

    /**
     * Obtains the text with joined lines separated by {@linkplain System#lineSeparator()
     * line separator}.
     */
    @Override
    public String toString() {
        return value;
    }

    @Override
    public Iterator<String> iterator() {
        return lines.stream().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Text)) {
            return false;
        }
        var strings = (Text) o;
        return Objects.equals(value, strings.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Splits the passed text separated with {@linkplain System#lineSeparator()
     * line separator} into lines.
     *
     * @return unmodifiable list of code lines
     */
    public static List<String> split(String text) {
        checkNotNull(text);
        return splitter().splitToList(text);
    }

    /**
     * Join the lines separated with {@linkplain System#lineSeparator()
     * line separator}.
     */
    public static String join(Iterable<String> lines) {
        checkNotNull(lines);
        return joiner().join(lines);
    }

    /**
     * Join the lines separating them with {@linkplain System#lineSeparator()
     * line separator}.
     */
    public static String join(String[] lines) {
        checkNotNull(lines);
        return joiner().join(lines);
    }

    /**
     * Obtains the {@link Joiner} on {@linkplain System#lineSeparator()
     * line separator}.
     */
    public static Joiner joiner() {
        return JOINER;
    }

    /**
     * Obtains the {@link Splitter} on {@link System#lineSeparator()}.
     */
    public static Splitter splitter() {
        return SPLITTER;
    }
}
