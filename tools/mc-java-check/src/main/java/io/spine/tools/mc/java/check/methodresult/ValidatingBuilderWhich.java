/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.check.methodresult;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.predicates.TypePredicates;
import com.google.protobuf.Message;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Type;

import java.util.regex.Pattern;

import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;

/**
 * A predicate which matches builders Protobuf messages.
 *
 * <p>Any Java class which descends from {@link Message.Builder} matches this predicate.
 */
final class ValidatingBuilderWhich implements TypePredicate {

    private static final long serialVersionUID = 0L;

    private static final Pattern SIDE_EFFECT_METHOD_NAME =
            Pattern.compile("(set|add|put|merge|remove).+");

    private static final TypePredicate IS_MESSAGE_BUILDER =
            TypePredicates.isDescendantOf(Message.Builder.class.getName());

    /**
     * Prevents direct instantiation.
     */
    private ValidatingBuilderWhich() {
    }

    /**
     * Obtains an instance method invocation matcher for setter methods of Protobuf Builders.
     *
     * <p>Matches methods starting from {@code set}, {@code add}, {@code put}, or {@code merge}.
     */
    static Matcher<ExpressionTree> callsSetterMethod() {
        return instanceMethod()
                .onClass(new ValidatingBuilderWhich())
                .withNameMatching(SIDE_EFFECT_METHOD_NAME);
    }

    @Override
    public boolean apply(Type type, VisitorState state) {
        return IS_MESSAGE_BUILDER.apply(type, state);
    }
}
