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

package io.spine.tools.check.vbuild;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.protobuf.MessageOrBuilder;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import io.spine.tools.check.BugPatternMatcher;

import static com.google.errorprone.matchers.Matchers.isSubtypeOf;

/**
 * A context-sensitive matcher.
 *
 * <p>If the matching element lies in a {@code Message} class or in {@code Builder} class, it is
 * never matched.
 */
interface ContextualMatcher<T extends Tree> extends BugPatternMatcher<T> {

    /**
     * Applies this matcher.
     *
     * <p>It is guaranteed that when this method is called the matcher is not in a {@code Message}
     * or a {@code Builder} class.
     *
     * @see #inMessageOrBuilder(VisitorState)
     */
    boolean outsideMessageContextMatches(T tree, VisitorState state);

    /**
     * {@inheritDoc}
     *
     * <p>If in the context of a {@code Message} or a {@code Builder}, always returns
     * {@code false}.
     *
     * @param tree
     *         the expression {@code Tree}
     * @param state
     *         the current {@code VisitorState}
     * @return {@code true} if {@link #inMessageOrBuilder} is {@code false} and
     *         {@link #outsideMessageContextMatches} is {@code true}; {@code false} otherwise
     */
    @Override
    default boolean matches(T tree, VisitorState state) {
        return !inMessageOrBuilder(state) && outsideMessageContextMatches(tree, state);
    }

    /**
     * Checks if the matcher is in the context of a {@code Message} or a {@code Builder}.
     *
     * @return {@code true} if currently matching statements inside a {@code Message} or
     *         a {@code Builder} descendant; {@code false} otherwise
     */
    default boolean inMessageOrBuilder(VisitorState state) {
        Matcher<ClassTree> messageOrBuilder = isSubtypeOf(MessageOrBuilder.class);
        ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
        return messageOrBuilder.matches(enclosingClass, state);
    }
}
