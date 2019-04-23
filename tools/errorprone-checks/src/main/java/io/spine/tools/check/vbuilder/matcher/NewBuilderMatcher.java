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

package io.spine.tools.check.vbuilder.matcher;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import io.spine.annotation.Internal;
import io.spine.tools.check.BugPatternMatcher;
import io.spine.tools.check.Fixer;
import io.spine.tools.check.vbuilder.fixer.NewBuilderFixer;

import static io.spine.protobuf.Messages.METHOD_NEW_BUILDER;

/**
 * A matcher for the {@link io.spine.tools.check.vbuilder.UseValidatingBuilder} bug pattern which
 * tracks down the cases where the {@code Message.newBuilder()} or the
 * {@code Message.newBuilder(prototype)} statement is used.
 *
 * <p>Both normally called and static-imported methods are handled.
 */
@Internal
public class NewBuilderMatcher implements BugPatternMatcher<MethodInvocationTree> {

    private final Matcher<ExpressionTree> matcher =
            CustomProtobufType.callingStaticMethod(METHOD_NEW_BUILDER);
    private final Fixer<MethodInvocationTree> fixer = new NewBuilderFixer();

    @Override
    public boolean matches(MethodInvocationTree tree, VisitorState state) {
        boolean matches = matcher.matches(tree, state);
        return matches;
    }

    @Override
    public Fixer<MethodInvocationTree> getFixer() {
        return fixer;
    }
}
