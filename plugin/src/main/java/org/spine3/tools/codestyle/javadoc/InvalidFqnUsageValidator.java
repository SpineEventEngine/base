/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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
package org.spine3.tools.codestyle.javadoc;

import com.google.common.base.Optional;
import org.spine3.tools.codestyle.AbstractCodeStyleFileValidator;
import org.spine3.tools.codestyle.CodeStyleViolation;
import org.spine3.tools.codestyle.StepConfiguration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.compile;

/**
 * Javadoc comments checker that validates the links wrong format usage.
 * In case if any violation is found it will be logged as warning in build's
 * stacktrace info or an error will be thrown. That depends on threshold and report type parameters
 * stated in build file.
 *
 * @author Alexander Aleksandrov
 */
public class InvalidFqnUsageValidator extends AbstractCodeStyleFileValidator {

    private final StepConfiguration configuration;

    public InvalidFqnUsageValidator(StepConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    protected InvalidResultStorage createStorage() {
        return new InvalidResultStorage();
    }

    @Override
    protected void checkViolationsCount() {
        if (getStorage().getContent()
                        .size() > configuration.getThreshold()
                                               .getValue()) {
            onAboveThreshold();
        }
    }

    /**
     * Describes the behavior in case if threshold is exceeded.
     */
    private void onAboveThreshold() {
        getStorage().logViolations();
        configuration.getReportType()
                     .logOrFail(new InvalidFqnUsageException());
    }

    @Override
    public List<CodeStyleViolation> checkForViolations(List<String> list) {
        int lineNumber = 0;
        final List<CodeStyleViolation> invalidLinks = newArrayList();
        for (String line : list) {
            final Optional<CodeStyleViolation> result = checkSingleComment(line);
            lineNumber++;
            if (result.isPresent()) {
                final CodeStyleViolation codeStyleViolation = result.get();
                codeStyleViolation.setIndex(lineNumber);
                invalidLinks.add(codeStyleViolation);
            }
        }
        return invalidLinks;
    }

    private static Optional<CodeStyleViolation> checkSingleComment(String comment) {
        final Matcher matcher = InvalidFqnUsageValidator.JavadocPattern.LINK.getPattern()
                                                                            .matcher(comment);
        final boolean found = matcher.find();
        if (found) {
            final String improperUsage = matcher.group(0);
            final CodeStyleViolation result = new CodeStyleViolation(improperUsage);
            return Optional.of(result);
        }
        return Optional.absent();
    }

    private enum JavadocPattern {

        /*
         * This regexp matches every link or linkplain in javadoc that is not in the format of
         * {@link <FQN> <text>} or {@linkplain <FQN> <text>}.
         *
         * Wrong links: {@link org.spine3.base.Client} or {@linkplain com.guava.AnyClass }
         * Correct links: {@link Class.InternalClass}, {@link org.spine3.base.Client Client},
         * {@linkplain org.spine3.base.Client some client class}
         *
         * 1st Capturing Group "(\{@link|\{@linkplain)"
         * 1st Alternative "\{@link"
         * "\{" matches the character "{" literally (case sensitive)
         * "@link" matches the characters "@link" literally (case sensitive)
         * 2nd Alternative "\{@linkplain"
         * "\{" matches the character "{" literally (case sensitive)
         * "@linkplain" matches the characters "@linkplain" literally (case sensitive)
         * " *" matches the character " " literally (case sensitive)
         * "*" Quantifier — Matches between zero and unlimited times, as many times as possible,
         * giving back as needed (greedy)

         * 2nd Capturing Group "((?!-)[a-z0-9-]{1,63}\.)"
         * Negative Lookahead "(?!-)"
         * Assert that the Regex below does not match
         * "-"matches the character "-" literally (case sensitive)
         * Match a single character present in the list below "[a-z0-9-]{1,63}"
         * "{1,63}" Quantifier — Matches between 1 and 63 times, as many times as possible,
         * giving back as needed (greedy)
         * "a-z" a single character in the range between "a" (ASCII 97) and "z" (ASCII 122)
         * (case sensitive)
         * "0-9" a single character in the range between "0" (ASCII 48) and "9" (ASCII 57)
         * (case sensitive)
         * "-" matches the character "-" literally (case sensitive)
         * "\." matches the character "." literally (case sensitive)

         * 3rd Capturing Group "((?!-)[a-zA-Z0-9-]{1,63}[a-zA-Z0-9-]\.)+"
         * "+" Quantifier — Matches between one and unlimited times, as many times as possible,
         * giving back as needed (greedy)
         * A repeated capturing group will only capture the last iteration.
         * Put a capturing group around the repeated group to capture all iterations or use a
         * non-capturing group instead if you're not interested in the data.

         * 4th Capturing Group "(\}|\ *\})"
         * 1st Alternative "\}"
         * "\}" matches the character "}" literally (case sensitive)
         * 2nd Alternative "\ *\}"
         * "\ *" matches the character " " literally (case sensitive)
         *  "*" Quantifier — Matches between zero and unlimited times, as many times as possible,
         *  giving back as needed (greedy)
         * "\}" matches the character "}" literally (case sensitive)
         */
        LINK(compile("(\\{@link|\\{@linkplain) *((?!-)[a-z0-9-]{1,63}\\.)((?!-)[a-zA-Z0-9-]{1,63}[a-zA-Z0-9-]\\.)+[a-zA-Z]{2,63}(\\}|\\ *\\})"));

        private final Pattern pattern;

        JavadocPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        Pattern getPattern() {
            return pattern;
        }

    }
}
