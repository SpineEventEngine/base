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

package io.spine.tools.gradle.compiler.protoc;

import org.checkerframework.checker.regex.qual.Regex;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates {@link FilePattern} selectors.
 **/
public final class FilePatternFactory {

    static final FilePatternFactory INSTANCE = new FilePatternFactory();

    /** Prevents direct instantiation. **/
    private FilePatternFactory() {
    }

    /**
     * Creates a {@link PostfixPattern} selector out of a supplied {@code postfix}.
     */
    public PostfixPattern endsWith(@Regex String postfix) {
        checkNotNull(postfix);
        PostfixPattern result = new PostfixPattern(postfix);
        return result;
    }

    /**
     * Creates a {@link PrefixPattern} selector out of a supplied {@code prefix}.
     */
    public PrefixPattern startsWith(@Regex String prefix) {
        checkNotNull(prefix);
        PrefixPattern result = new PrefixPattern(prefix);
        return result;
    }

    /**
     * Creates a {@link RegexPattern} selector out of a supplied {@code regex}.
     */
    public RegexPattern regex(@Regex String regex) {
        checkNotNull(regex);
        RegexPattern result = new RegexPattern(regex);
        return result;
    }
}
