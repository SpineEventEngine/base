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

package io.spine.tools.check.vbuilder.matcher;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.suppliers.Supplier;
import com.google.protobuf.Message;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import io.spine.annotation.Internal;
import io.spine.tools.check.BugPatternMatcher;
import io.spine.tools.check.Fixer;
import io.spine.tools.check.vbuilder.fixer.NewBuilderFixer;

import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static com.google.errorprone.suppliers.Suppliers.typeFromClass;
import static com.google.errorprone.util.ASTHelpers.isSubtype;

/**
 * A matcher for the {@link io.spine.tools.check.vbuilder.UseValidatingBuilder} bug pattern which
 * tracks down the cases where {@code Message.newBuilder()} or {@code Message.newBuilder(prototype)}
 * construction is used.
 *
 * <p>Both normally called and static-imported methods are handled.
 *
 * @author Dmytro Kuzmin
 */
@Internal
public class NewBuilderMatcher implements BugPatternMatcher<MethodInvocationTree> {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Commonly used method name.
    private static final String METHOD_NAME = "newBuilder";

    private final Matcher<ExpressionTree> matcher = matcher();
    private final Fixer<MethodInvocationTree> fixer = new NewBuilderFixer();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(MethodInvocationTree tree, VisitorState state) {
        boolean matches = matcher.matches(tree, state);
        return matches;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fixer<MethodInvocationTree> getFixer() {
        return fixer;
    }

    private static Matcher<ExpressionTree> matcher() {
        TypePredicate messageSubtype = (type, state) -> {
            Supplier<Type> typeSupplier = typeFromClass(Message.class);
            Type messageClassAsType = typeSupplier.get(state);
            boolean isMessageSubclass = isSubtype(type, messageClassAsType, state);
            return isMessageSubclass;
        };
        MethodNameMatcher matcher = staticMethod().onClass(messageSubtype)
                                                  .named(METHOD_NAME);
        return matcher;
    }
}
