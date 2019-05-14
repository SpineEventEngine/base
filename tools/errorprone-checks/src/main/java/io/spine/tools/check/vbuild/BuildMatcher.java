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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.protobuf.MessageOrBuilder;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import io.spine.tools.check.BugPatternMatcher;
import io.spine.tools.check.Fixer;

import static com.google.errorprone.matchers.Matchers.isSubtypeOf;

/**
 * A matcher for the {@link io.spine.tools.check.vbuilder.UseValidatingBuilder} bug pattern which
 * tracks down the cases where the {@code Message.newBuilder()} or the
 * {@code Message.newBuilder(prototype)} statement is used.
 *
 * <p>Both normally called and static-imported methods are handled.
 */
enum BuildMatcher implements BugPatternMatcher<MethodInvocationTree> {

    INSTANCE;

    @SuppressWarnings("DuplicateStringLiteralInspection") // Used in another context.
    private static final String BUILD_METHOD_NAME = "build";

    @SuppressWarnings("ImmutableEnumChecker")
    private static final Matcher<ExpressionTree> builderBuild =
            GeneratedValidatingBuilder.callingInstanceMethod(BUILD_METHOD_NAME);
    private static final Matcher<ClassTree> messageOrBuilder = isSubtypeOf(MessageOrBuilder.class);

    @Override
    public boolean matches(MethodInvocationTree tree, VisitorState state) {
        boolean matches = builderBuild.matches(tree, state)
                      && !notInMessageOrBuilder(state);
        return matches;
    }

    private static boolean notInMessageOrBuilder(VisitorState state) {
        ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
        return messageOrBuilder.matches(enclosingClass, state);
    }

    @Override
    public ImmutableList<Fixer<MethodInvocationTree>> fixers() {
        return ImmutableList.copyOf(BuildWarningFixer.values());
    }
}
