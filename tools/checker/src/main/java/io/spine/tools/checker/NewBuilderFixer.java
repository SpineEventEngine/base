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

package io.spine.tools.checker;

import com.google.common.collect.Iterators;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.protobuf.Message;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import io.spine.protobuf.Messages;

import java.util.List;
import java.util.Optional;

import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static com.google.errorprone.suppliers.Suppliers.typeFromClass;
import static com.google.errorprone.util.ASTHelpers.isSubtype;

class NewBuilderFixer extends BuilderCallFixer {

    private static final String METHOD_NAME = Messages.METHOD_NEW_BUILDER;

    @Override
    public boolean matches(MethodInvocationTree tree, VisitorState state) {
        boolean methodNameMatches = newBuilderMatcher().matches(tree, state);
        // todo refactor into something decent
        ExpressionTree methodSelect = tree.getMethodSelect();
        boolean calledOnClass = methodSelect instanceof JCFieldAccess;
        boolean staticImported = methodSelect instanceof JCIdent;
        if (!methodNameMatches || (!calledOnClass && !staticImported)) {
            return false;
        }
        Type type = getTypeOnWhichInvoked(tree);
        Supplier<Type> typeSupplier = typeFromClass(Message.class);
        Type messageClassAsType = typeSupplier.get(state);
        boolean invokedOnMessage = isSubtype(type, messageClassAsType, state);
        return invokedOnMessage;
    }

    @Override
    public Optional<Fix> buildFix(MethodInvocationTree tree, VisitorState state) {
        List<? extends ExpressionTree> methodCallArgs = tree.getArguments();
        if (methodCallArgs.isEmpty()) {
            return fixForNoArgs(tree, state);
        }
        if (methodCallArgs.size() == 1) {
            return fixForOneArg(tree, state);
        }
        return noFix();
    }

    private static Matcher<ExpressionTree> newBuilderMatcher() {
        MethodNameMatcher matcher = staticMethod().anyClass()
                                                  .named(METHOD_NAME);
        return matcher;
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
