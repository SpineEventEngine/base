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

package io.spine.tools.check;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.sun.source.tree.Tree;
import io.spine.annotation.Internal;

/**
 * A basic interface to match the given expression against some known case of the
 * {@link com.google.errorprone.BugPattern}.
 *
 * @param <T> the expression {@code Tree}
 */
@Internal
public interface BugPatternMatcher<T extends Tree> {

    /**
     * Checks if the given expression matches the {@link com.google.errorprone.BugPattern} case
     * processed by this class.
     *
     * <p>The method should be used from inside the
     * {@link com.google.errorprone.bugpatterns.BugChecker} implementations, so the Error Prone
     * scanners provide the proper {@code Tree} and {@code VisitorState} corresponding to the
     * currently assessed expression.
     *
     * @param tree  the expression {@code Tree}
     * @param state the current {@code VisitorState}
     * @return {@code true} if the expression matches the bug pattern and {@code false} otherwise
     */
    boolean matches(T tree, VisitorState state);

    /**
     * Obtains {@code Fix}es for the case of the {@link com.google.errorprone.BugPattern}
     * processed by this class.
     *
     * @return {@code Fix}es for the processed bug pattern case
     */
    ImmutableList<Fix> fixes(T tree);
}
