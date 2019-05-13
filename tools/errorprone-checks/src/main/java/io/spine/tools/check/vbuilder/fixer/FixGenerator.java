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
import io.spine.type.MessageType;

import static com.google.errorprone.fixes.SuggestedFixes.prettyType;
import static com.google.errorprone.util.ASTHelpers.enclosingClass;
import static com.sun.source.tree.Tree.Kind.IDENTIFIER;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A generator for the common {@link com.google.errorprone.BugPattern} {@linkplain Fix fixes}
 * related to the {@link io.spine.validate.ValidatingBuilder} usage.
 *
 * <p>This class should only be used from the Error Prone
 * {@link com.google.errorprone.bugpatterns.BugChecker} context, where the code scanners can provide
 * proper {@link MethodInvocationTree} and {@link VisitorState} for its initialization.
 *
 * @see io.spine.tools.check.vbuilder.UseValidatingBuilder
 */
class FixGenerator {

    private final MethodInvocationTree tree;
    private final VisitorState state;

    private FixGenerator(MethodInvocationTree tree, VisitorState state) {
        this.tree = tree;
        this.state = state;
    }

    /**
     * Creates the {@code FixGenerator} instance for the given expression and visitor state.
     *
     * @param tree  the expression {@code Tree}
     * @param state the current {@code VisitorState}
     * @return the {@code FixGenerator} instance for the given expression
     */
    static FixGenerator createFor(MethodInvocationTree tree, VisitorState state) {
        return new FixGenerator(tree, state);
    }

    /**
     * Creates a fix which replaces the current expression with the {@code ...VBuilder.newBuilder()}
     * expression.
     *
     * <p>This method assumes that the {@linkplain #tree current expression} is the call on some of
     * the {@link com.google.protobuf.Message} class descendants.
     *
     * @return the {@code Fix} which can be later displayed to the user via the Error Prone tools
     */
    Fix newVBuilderCall() {
        String newVBuilderCall = ".newBuilder()";
        Fix fix = callOnVBuilder(newVBuilderCall);
        return fix;
    }

    /**
     * Creates a fix which replaces the current expression with the
     * {@code ...VBuilder.newBuilder().mergeFrom(arg)} expression.
     *
     * <p>This method assumes that the {@linkplain #tree current expression} is the call that
     * utilizes some of the {@link com.google.protobuf.Message} class instances for the field
     * initialization.
     *
     * @param mergeFromArg the object from which the fields are taken for the
     *                     {@link com.google.protobuf.Message.Builder}
     * @return the {@code Fix} which can be later displayed to the user via the Error Prone tools
     */
    Fix mergeFromCall(String mergeFromArg) {
        String mergeFromCall = ".newBuilder().mergeFrom(" + mergeFromArg + ')';
        Fix fix = callOnVBuilder(mergeFromCall);
        return fix;
    }

    /**
     * Generates an expression such that given {@code statement} is called on the
     * {@link io.spine.validate.ValidatingBuilder} class.
     *
     * <p>The {@code ValidatingBuilder} class is calculated from the current expression.
     *
     * @param statement the statement to call on the validating builder class
     * @return the statement with the call
     */
    private Fix callOnVBuilder(String statement) {
        String vBuilderName = generateVBuilderName();
        String fixedLine = vBuilderName + statement;
        Fix fix = SuggestedFix.builder()
                              .replace(tree, fixedLine)
                              .build();
        return fix;
    }

    /**
     * Generates the {@link io.spine.validate.ValidatingBuilder} class name based on the current
     * expression {@code Tree} and {@code VisitorState}.
     */
    private String generateVBuilderName() {
        Type type = getTypeOnWhichInvoked();
        String simpleName = prettyType(state, null, type);
        String vBuilderName = simpleName + MessageType.VBUILDER_SUFFIX;
        return vBuilderName;
    }

    /**
     * Obtains the {@code Type} on which the current expression is invoked.
     */
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
