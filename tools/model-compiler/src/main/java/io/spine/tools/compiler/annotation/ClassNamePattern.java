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

package io.spine.tools.compiler.annotation;

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
final class ClassNamePattern {

    private final Pattern pattern;

    private ClassNamePattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Creates a new instance of {@code ClassNamePattern} based on the given regular expression.
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
}
