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

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import com.google.protobuf.Message;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

import java.util.Optional;

import static com.google.errorprone.matchers.Matchers.instanceMethod;

class NewBuilderOfTypeFixer extends BuilderCallFixer {

    private static final String METHOD_NAME = "newBuilderForType";

    @Override
    public boolean matches(MethodInvocationTree tree, VisitorState state) {
        boolean isNewBuilderForClassCall = newBuilderForTypeMatcher().matches(tree, state);
        return isNewBuilderForClassCall;
    }

    @Override
    public Optional<Fix> buildFix(MethodInvocationTree tree, VisitorState state) {
        Fix fix = newVBuilderCall(tree, state);
        Optional<Fix> result = Optional.of(fix);
        return result;
    }

    private static Matcher<ExpressionTree> newBuilderForTypeMatcher() {
        String messageClassName = Message.class.getName();
        MethodNameMatcher matcher = instanceMethod().onDescendantOf(messageClassName)
                                                    .named(METHOD_NAME);
        return matcher;
    }

}
