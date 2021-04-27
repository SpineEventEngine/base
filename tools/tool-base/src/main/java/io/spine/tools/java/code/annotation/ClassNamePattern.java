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

package io.spine.tools.java.code.annotation;

import com.google.common.base.Objects;
import io.spine.code.java.ClassName;
import org.checkerframework.checker.regex.qual.Regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * A pattern matching qualified names of Java classes.
 *
 * <p>The pattern is based on the Java {@linkplain java.util.regex.Pattern regular expression}.
 */
public final class ClassNamePattern {

    private final Pattern pattern;

    private ClassNamePattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Creates a new instance from the passed regular expression.
     *
     * <p>The regex does not receive additional flags.
     *
     * @param regex
     *         the regex to match class names
     * @return new {@code ClassNamePattern}
     */
    static ClassNamePattern compile(@Regex String regex) {
        checkArgument(!isNullOrEmpty(regex));
        Pattern pattern = Pattern.compile(regex);
        return new ClassNamePattern(pattern);
    }

    /**
     * Tries to match the given class name against this pattern.
     *
     * @param name
     *         the class name to match
     * @return {@code true} if the {@code ClassName}
     *         {@linkplain java.util.regex.Matcher#matches() matches} this pattern
     */
    boolean matches(ClassName name) {
        checkNotNull(name);
        Matcher matcher = pattern.matcher(name.value());
        return matcher.matches();
    }

    @Override
    public String toString() {
        return pattern.pattern();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Does not account for the <strong>effective</strong> regex pattern equality. If two
     * patterns are constructed differently, they are <strong>not</strong> equal according to this
     * method. Only equal pattern string literals yield equal patterns.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassNamePattern pattern1 = (ClassNamePattern) o;
        return Objects.equal(pattern.pattern(), pattern1.pattern.pattern());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pattern);
    }
}
