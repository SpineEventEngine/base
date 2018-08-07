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

package io.spine.tools.check.vbuilder.fixer;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;

import static com.google.errorprone.fixes.SuggestedFixes.prettyType;
import static com.google.errorprone.util.ASTHelpers.enclosingClass;
import static com.sun.source.tree.Tree.Kind.IDENTIFIER;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static io.spine.util.Exceptions.newIllegalStateException;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Method names for which introducing constant doesn't seem reasonable.
class FixGenerator {

    private final MethodInvocationTree tree;
    private final VisitorState state;

    private FixGenerator(MethodInvocationTree tree, VisitorState state) {
        this.tree = tree;
        this.state = state;
    }

    static FixGenerator createFor(MethodInvocationTree tree, VisitorState state) {
        return new FixGenerator(tree, state);
    }

    Fix newVBuilderCall() {
        String newVBuilderCall = ".newBuilder()";
        Fix fix = callOnVBuilder(newVBuilderCall);
        return fix;
    }

    Fix mergeFromCall(String mergeFromArg) {
        String mergeFromCall = ".newBuilder().mergeFrom(" + mergeFromArg + ')';
        Fix fix = callOnVBuilder(mergeFromCall);
        return fix;
    }

    private Fix callOnVBuilder(String statement) {
        String vBuilderName = generateVBuilderName();
        String fixedLine = vBuilderName + statement;
        Fix fix = SuggestedFix.builder()
                              .replace(tree, fixedLine)
                              .build();
        return fix;
    }

    private String generateVBuilderName() {
        Type type = getTypeOnWhichInvoked();
        String simpleName = prettyType(state, null, type);
        String vBuilderName = simpleName + "VBuilder";
        return vBuilderName;
    }

    private Type getTypeOnWhichInvoked() {
        ExpressionTree expression = tree.getMethodSelect();
        Kind kind = expression.getKind();
        if (kind == MEMBER_SELECT) {
            return typeFromMethodCall((JCFieldAccess) expression);
        }
        if (kind == IDENTIFIER) {
            return typeFromStaticImportedCall((JCIdent) expression);
        }
        throw newIllegalStateException("Expression of unexpected kind %s where method call " +
                                               "or static-imported method call are expected",
                                       expression.getKind());
    }

    private static Type typeFromMethodCall(JCFieldAccess expression) {
        JCExpression invokedOn = expression.selected;
        Type type = invokedOn.type;
        return type;
    }

    private static Type typeFromStaticImportedCall(JCIdent expression) {
        MethodSymbol method = (MethodSymbol) expression.sym;
        ClassSymbol classSymbol = enclosingClass(method);
        Type type = classSymbol.type;
        return type;
    }
}
