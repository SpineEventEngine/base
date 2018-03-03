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
package io.spine.tools.codestyle.rightmargin;

import com.google.common.base.Optional;
import io.spine.tools.codestyle.AbstractJavaStyleCheck;
import io.spine.tools.codestyle.CodeStyleViolation;
import io.spine.tools.codestyle.StepConfiguration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;

/**
 * It checks files for the lines that are going out of the right margin value, specified by
 * threshold. In case if any violation is found it will be logged as warning in the build
 * stacktrace info.
 *
 * @author Alexander Aleksandrov
 * @author Alexander Yevsyukov
 */
public class RightMarginCheck extends AbstractJavaStyleCheck {

    private static final Pattern IMPORT_OR_LINK = Pattern.compile("import|<a href");

    private final StepConfiguration configuration;

    RightMarginCheck(StepConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    protected InvalidLineStorage createStorage(){
         return new InvalidLineStorage();
    }

    @Override
    public List<CodeStyleViolation> findViolations(List<String> fileContent) {
        int lineNumber = 0;
        final List<CodeStyleViolation> invalidLines = newArrayList();
        for (String line : fileContent) {
            final Optional<CodeStyleViolation> result = checkLine(line);
            lineNumber++;
            if (result.isPresent()) {
                final CodeStyleViolation codeStyleViolation = result.get()
                                                                    .withLineNumber(lineNumber);
                invalidLines.add(codeStyleViolation);
            }
        }
        return invalidLines;
    }

    @Override
    protected void processResult() {
        getStorage().logViolations();
    }

    private Optional<CodeStyleViolation> checkLine(String line) {
        final Matcher matcher = IMPORT_OR_LINK.matcher(line);
        final boolean found = matcher.find();
        if (found) {
            return Optional.absent();
        }
        final int maxTextWidth = configuration.getMaxTextWidth();
        if (line.length() > maxTextWidth) {
            final CodeStyleViolation result = new CodeStyleViolation(line);
            return Optional.of(result);
        }
        return Optional.absent();
    }
}
