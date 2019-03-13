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

import io.spine.code.proto.FileName;
import org.checkerframework.checker.regex.qual.Regex;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates {@link FileSelector} selectors.
 **/
public final class FileSelectorFactory {

    static final FileSelectorFactory INSTANCE = new FileSelectorFactory();

    /** Prevents direct instantiation. **/
    private FileSelectorFactory() {
    }

    /**
     * Creates a {@link PostfixSelector} selector out of a supplied {@code postfix}.
     */
    public PostfixSelector endsWith(@Regex String postfix) {
        checkNotNull(postfix);
        PostfixSelector result = new PostfixSelector(postfix);
        return result;
    }

    /**
     * Creates a {@link PrefixSelector} selector out of a supplied {@code prefix}.
     */
    public PrefixSelector startsWith(@Regex String prefix) {
        checkNotNull(prefix);
        PrefixSelector result = new PrefixSelector(prefix);
        return result;
    }

    /**
     * Creates a {@link RegexSelector} selector out of a supplied {@code regex}.
     */
    public RegexSelector matches(@Regex String regex) {
        checkNotNull(regex);
        RegexSelector result = new RegexSelector(regex);
        return result;
    }

    /**
     * Creates a {@link PostfixSelector} selector that matches {@code all} Protobuf files.
     *
     * <p>It is expected that a Protobuf file ends with {@link FileName#EXTENSION .proto} extension.
     */
    public PostfixSelector all() {
        PostfixSelector result = new PostfixSelector(FileName.EXTENSION);
        return result;
    }
}
