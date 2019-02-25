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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.regex.qual.Regex;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates particular {@link FilePattern} selectors.
 *
 * @param <T>
 *         Protobuf configuration counterpart
 * @param <P>
 *         postfix pattern selector
 */
public abstract class FilePatternFactory<T extends Message, P extends PostfixPattern<T>> {

    private final Set<FilePattern<T>> patterns;

    FilePatternFactory() {
        this.patterns = Sets.newConcurrentHashSet();
    }

    /**
     * Creates a {@link PostfixPattern} selector out of a supplied {@code postfix}.
     */
    public P endsWith(@Regex String postfix) {
        checkNotNull(postfix);
        P result = newPostfixPattern(postfix);
        if (!patterns.add(result)){
            patterns.remove(result);
            patterns.add(result);
        }
        return result;
    }

    /**
     * Returns currently configured file patterns.
     */
    @Internal
    ImmutableSet<FilePattern<T>> patterns() {
        return ImmutableSet.copyOf(patterns);
    }

    /**
     * Instantiates a particular {@link PostfixPattern} for the supplied {@code postfix}.
     */
    @Internal
    abstract P newPostfixPattern(@NonNull @Regex String postfix);
}
