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

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.protobuf.Message;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import io.spine.tools.check.BugPatternMatcher;
import io.spine.tools.check.Fixer;

import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static com.google.errorprone.suppliers.Suppliers.typeFromClass;
import static com.google.errorprone.util.ASTHelpers.enclosingClass;
import static com.google.errorprone.util.ASTHelpers.isSubtype;

public class NewBuilderMatcher implements BugPatternMatcher<MethodInvocationTree> {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Commonly used method name.
    private static final String METHOD_NAME = "newBuilder";

    private final Fixer<MethodInvocationTree> fixer = new NewBuilderFixer();

    @Override
    public boolean matches(MethodInvocationTree tree, VisitorState state) {
        boolean methodNameMatches = methodNameMatcher().matches(tree, state);
        ExpressionTree methodSelect = tree.getMethodSelect();
        boolean calledOnClass = methodSelect instanceof JCTree.JCFieldAccess;
        boolean staticImported = methodSelect instanceof JCTree.JCIdent;
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
    public Fixer<MethodInvocationTree> getFixer() {
        return fixer;
    }

    Type getTypeOnWhichInvoked(MethodInvocationTree tree) {
        ExpressionTree expression = tree.getMethodSelect();
        if (expression instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) expression;
            JCTree.JCExpression invokedOn = fieldAccess.selected;
            return invokedOn.type;
        }

        // Static imported method.
        JCTree.JCIdent ident = (JCTree.JCIdent) expression;
        Symbol.MethodSymbol method = (Symbol.MethodSymbol) ident.sym;
        Symbol.ClassSymbol classSymbol = enclosingClass(method);
        return classSymbol.type;
    }

    private static Matcher<ExpressionTree> methodNameMatcher() {
        MethodNameMatcher matcher = staticMethod().anyClass()
                                                  .named(METHOD_NAME);
        return matcher;
    }
}
