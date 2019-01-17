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

package io.spine.tools.check.vbuilder;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.protobuf.Message;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import io.spine.annotation.Internal;
import io.spine.tools.check.BugPatternMatcher;
import io.spine.tools.check.Fixer;
import io.spine.tools.check.vbuilder.matcher.NewBuilderForTypeMatcher;
import io.spine.tools.check.vbuilder.matcher.NewBuilderMatcher;
import io.spine.tools.check.vbuilder.matcher.ToBuilderMatcher;
import io.spine.validate.ValidatingBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;

/**
 * A custom Error Prone check that matches the usages of the ordinary
 * {@code Message.Builder} and advices using {@linkplain ValidatingBuilder Spine Validating Builder}
 * instead.
 *
 * <p>Currently, it detects the erroneous statements and suggests fixes for them as follows:
 *
 * <ul>
 * <li>{@code Message.newBuilder()} -&gt; {@code MessageVBuilder.newBuilder()}
 * <li>{@code Message.newBuilder(prototype)} -&gt;
 * {@code MessageVBuilder.newBuilder().mergeFrom(prototype)}
 * <li>{@code message.newBuilderForType()} -&gt; {@code MessageVBuilder.newBuilder()}
 * <li>{@code message.toBuilder()} -&gt; {@code MessageVBuilder.newBuilder().mergeFrom(message)}
 * </ul>
 *
 * <p>Usage of the {@code Message.Builder} inside of the generated {@code Message messages} and
 * in the {@linkplain ValidatingBuilder validating builders} themselves is allowed.
 */
@AutoService(BugChecker.class)
@BugPattern(
        name = "UseValidatingBuilder",
        category = JDK,
        summary = UseValidatingBuilder.SUMMARY,
        severity = WARNING,
        linkType = CUSTOM,
        link = UseValidatingBuilder.LINK
)
@Internal
public class UseValidatingBuilder extends BugChecker implements MethodInvocationTreeMatcher {

    static final String SUMMARY = "Prefer using Spine Validating Builders instead of the " +
            "ordinary Message Builders for the Protobuf Messages";

    static final String LINK =
            "https://github.com/SpineEventEngine/core-java/wiki/Validating-Builders-Generation";

    private static final long serialVersionUID = 0L;

    private static final List<BugPatternMatcher<MethodInvocationTree>> matchers = matchers();

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        for (BugPatternMatcher<MethodInvocationTree> matcher : matchers) {
            if (matcher.matches(tree, state) && !isInVBuilderOrMessage(state)) {
                Fixer<MethodInvocationTree> fixer = matcher.getFixer();
                Optional<Fix> fix = fixer.createFix(tree, state);
                Description description = describeMatch(tree, fix);
                return description;
            }
        }
        return NO_MATCH;
    }

    private static boolean isInVBuilderOrMessage(VisitorState state) {
        ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
        boolean isInVBuilder = vBuilderMatcher().matches(enclosingClass, state);
        boolean isInMessage = messageMatcher().matches(enclosingClass, state);
        return isInVBuilder || isInMessage;
    }

    private static Matcher<Tree> vBuilderMatcher() {
        Matcher<Tree> matcher = isSubtypeOf(ValidatingBuilder.class);
        return matcher;
    }

    private static Matcher<Tree> messageMatcher() {
        Matcher<Tree> matcher = isSubtypeOf(Message.class);
        return matcher;
    }

    private static List<BugPatternMatcher<MethodInvocationTree>> matchers() {
        List<BugPatternMatcher<MethodInvocationTree>> matchers = new ArrayList<>();
        matchers.add(new NewBuilderMatcher());
        matchers.add(new NewBuilderForTypeMatcher());
        matchers.add(new ToBuilderMatcher());
        return matchers;
    }
}
