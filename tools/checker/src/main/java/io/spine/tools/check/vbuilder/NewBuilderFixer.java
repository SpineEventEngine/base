/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.check.vbuilder;

import com.google.common.collect.Iterators;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

import java.util.List;
import java.util.Optional;

class NewBuilderFixer extends BuilderCallFixer {

    @Override
    public Optional<Fix> createFix(MethodInvocationTree tree, VisitorState state) {
        List<? extends ExpressionTree> methodCallArgs = tree.getArguments();
        if (methodCallArgs.isEmpty()) {
            return fixForNoArgs(tree, state);
        }
        if (methodCallArgs.size() == 1) {
            return fixForOneArg(tree, state);
        }
        return noFix();
    }

    private Optional<Fix> fixForNoArgs(MethodInvocationTree tree, VisitorState state) {
        Fix fix = newVBuilderCall(tree, state);
        Optional<Fix> result = Optional.of(fix);
        return result;
    }

    private Optional<Fix> fixForOneArg(MethodInvocationTree tree, VisitorState state) {
        List<? extends ExpressionTree> args = tree.getArguments();
        ExpressionTree arg = Iterators.getOnlyElement(args.iterator());
        String argString = arg.toString();
        Fix fix = mergeFromCall(tree, state, argString);
        Optional<Fix> result = Optional.of(fix);
        return result;
    }

    private static Optional<Fix> noFix() {
        return Optional.empty();
    }
}
