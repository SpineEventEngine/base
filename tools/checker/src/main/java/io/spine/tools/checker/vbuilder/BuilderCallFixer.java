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

package io.spine.tools.checker.vbuilder;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import io.spine.tools.checker.SuggestedFixCreator;

import static com.google.errorprone.fixes.SuggestedFixes.prettyType;
import static com.google.errorprone.util.ASTHelpers.enclosingClass;

@SuppressWarnings("DuplicateStringLiteralInspection")
// Method names for which introducing constant doesn't seem reasonable.
abstract class BuilderCallFixer implements SuggestedFixCreator<MethodInvocationTree> {

    Fix newVBuilderCall(MethodInvocationTree tree, VisitorState state) {
        SuggestedFix.Builder builder = SuggestedFix.builder();
        String vBuilderName = generateVBuilderName(tree, state, builder);
        String vBuilderCall = vBuilderName + ".newBuilder()";
        builder.replace(tree, vBuilderCall);
        SuggestedFix fix = builder.build();
        return fix;
    }

    Fix mergeFromCall(MethodInvocationTree tree, VisitorState state, String mergeFromArg) {
        SuggestedFix.Builder builder = SuggestedFix.builder();
        String vBuilderName = generateVBuilderName(tree, state, builder);
        builder.replace(tree, vBuilderName + ".newBuilder().mergeFrom(" + mergeFromArg + ')');
        SuggestedFix fix = builder.build();
        return fix;
    }

    Type getTypeOnWhichInvoked(MethodInvocationTree tree) {
        ExpressionTree expression = tree.getMethodSelect();
        if (expression instanceof JCFieldAccess) {
            JCFieldAccess fieldAccess = (JCFieldAccess) expression;
            JCExpression invokedOn = fieldAccess.selected;
            return invokedOn.type;
        }

        // Static imported method.
        JCIdent ident = (JCIdent) expression;
        MethodSymbol method = (MethodSymbol) ident.sym;
        ClassSymbol classSymbol = enclosingClass(method);
        return classSymbol.type;
    }

    private String generateVBuilderName(MethodInvocationTree tree,
                                        VisitorState state,
                                        SuggestedFix.Builder builder) {
        Type type = getTypeOnWhichInvoked(tree);
        String simpleName = prettyType(state, builder, type);
        String vBuilderName = simpleName + "VBuilder";
        return vBuilderName;
    }
}
