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
import com.google.errorprone.predicates.TypePredicate;
import com.google.protobuf.Message;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import io.spine.code.java.ClassName;

import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static com.google.errorprone.predicates.TypePredicates.isDescendantOf;
import static io.spine.code.GooglePackage.notInGooglePackage;

/**
 * A predicate which matches custom (i.e. non-Google) Protobuf types.
 *
 * <p>Any {@code final} Java class which descends from {@link Message} and does not belong
 * to {@code com.google} or {@code google} package or its subpackage matches this predicate.
 */
final class CustomProtobufType implements TypePredicate {

    private static final long serialVersionUID = 0L;

    private static final TypePredicate IS_MESSAGE = isDescendantOf(Message.class.getName());

    /**
     * Prevents direct instantiation.
     */
    private CustomProtobufType() {
    }

    /**
     * Obtains a static method invocation matcher for the methods in custom Protobuf types and with
     * the given name.
     *
     * @param methodName the method name to match
     */
    static Matcher<ExpressionTree> callingStaticMethod(String methodName) {
        return staticMethod()
                .onClass(new CustomProtobufType())
                .named(methodName);
    }

    /**
     * Obtains an instance method invocation matcher for the methods in custom Protobuf types and
     * with the given name.
     *
     * @param methodName the method name to match
     */
    static Matcher<ExpressionTree> callingInstanceMethod(String methodName) {
        return instanceMethod()
                .onClass(new CustomProtobufType())
                .named(methodName);
    }

    @Override
    public boolean apply(Type type, VisitorState state) {
        return type.isFinal()
            && IS_MESSAGE.apply(type, state)
            && notGoogle(type);
    }

    private static boolean notGoogle(Type type) {
        Symbol.TypeSymbol typeSymbol = type.asElement();
        String typeFqn = typeSymbol.getQualifiedName()
                                   .toString();
        return notInGooglePackage(ClassName.of(typeFqn));
    }
}
