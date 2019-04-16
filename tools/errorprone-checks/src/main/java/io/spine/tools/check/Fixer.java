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

package io.spine.tools.check;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.sun.source.tree.Tree;
import io.spine.annotation.Internal;

import java.util.Optional;

/**
 * Generates a {@link Fix} to be displayed to the user given the errored expression.
 *
 * @param <T> the expression {@code Tree}
 * @see com.google.errorprone.bugpatterns.BugChecker#describeMatch(Tree, Optional)
 */
@Internal
public interface Fixer<T extends Tree> {

    /**
     * Creates a fix for the {@link com.google.errorprone.BugPattern} given the position where the
     * error was found and the expression.
     *
     * <p>The method should be used in the {@link com.google.errorprone.bugpatterns.BugChecker}
     * implementations where the tree and the state are provided by the Error Prone code scanners.
     *
     * @param tree  the errored expression {@code Tree}
     * @param state the current {@code VisitorState}
     * @return the {@code Optional} containing the {@code Fix} or {@link Optional#EMPTY} if no fix
     *         can be created
     */
    Optional<Fix> createFix(T tree, VisitorState state);
}
