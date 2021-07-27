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

package io.spine.tools.check.methodresult;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.CheckReturnValue;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.not;

/**
 * An ErrorProne check which ensures that the values returned from methods are not
 * accidentally ignored.
 *
 * <p>This check is a substitute for {@link CheckReturnValue}. The difference is that this check
 * ignores invocations of mutating methods on message builders.
 *
 * <p>The check may be suppressed in the same ways as {@link CheckReturnValue}.
 */
@AutoService(BugChecker.class)
@BugPattern(
        name = "HandleMethodResult",
        altNames = {"CheckReturnValue", "ResultOfMethodCallIgnored", "ReturnValueIgnored"},
        summary = HandleMethodResult.SUMMARY,
        severity = ERROR,
        linkType = NONE
)
public final class HandleMethodResult extends CheckReturnValue {

    private static final long serialVersionUID = 0L;

    static final String SUMMARY =
            "Ignored return value of method that is annotated with `@CheckReturnValue`";

    @Override
    public Matcher<ExpressionTree> specializedMatcher() {
        Matcher<ExpressionTree> checkReturnValue = super.specializedMatcher();
        Matcher<ExpressionTree> notBuilderSetter = not(builderSetter());
        return allOf(checkReturnValue, notBuilderSetter);
    }

    private static Matcher<ExpressionTree> builderSetter() {
        return MessageBuilderWhich.callsSetterMethod();
    }
}
