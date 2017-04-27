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
package org.spine3.tools.codestyle.rightmargin;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.tools.codestyle.CodestyleFileValidator;
import org.spine3.tools.codestyle.CodestyleViolation;
import org.spine3.tools.codestyle.StepConfiguration;
import org.spine3.tools.codestyle.javadoc.InvalidFqnUsageException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.compile;

/**
 * It checks files for the lines that are going out of the right margin value, specified by
 * threshold. In case if any violation is found it will be logged as warning in build's
 * stacktrace info.
 *
 * @author Alexander Aleksandrov
 */
public class RightMarginValidator implements CodestyleFileValidator {
    private static final String JAVA_EXTENSION = ".java";

    private final InvalidLineStorage storage = new InvalidLineStorage();
    private final StepConfiguration configuration;

    public RightMarginValidator(StepConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void validate(Path path) throws InvalidLineLengthException {
        final List<String> content;
        if (!path.toString()
                 .endsWith(JAVA_EXTENSION)) {
            return;
        }
        try {
            content = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the contents of the file: " + path, e);
        }
        final List<CodestyleViolation> invalidLines = checkForViolations(content);
        if (!invalidLines.isEmpty()) {
            storage.save(path, invalidLines);
        }
        checkThreshold();
    }

    @Override
    public List<CodestyleViolation> checkForViolations(List<String> list) {
        int lineNumber = 0;
        final List<CodestyleViolation> invalidLines = newArrayList();
        for (String line : list) {
            final Optional<CodestyleViolation> result = checkSingleLine(line);
            lineNumber++;
            if (result.isPresent()) {
                final CodestyleViolation codestyleViolation = result.get();
                codestyleViolation.setIndex(lineNumber);
                invalidLines.add(codestyleViolation);
            }
        }
        return invalidLines;
    }

    @Override
    public void checkThreshold() {
            onAboveThreshold();
    }

    @Override
    public void onAboveThreshold() {
        storage.logInvalidLines();
        configuration.getReportType().logOrFail(new InvalidLineLengthException());
    }

    private Optional<CodestyleViolation> checkSingleLine(String line) {
        final Matcher matcher = RightMarginValidator.JavadocPattern.LINK.getPattern()
                                                                        .matcher(line);
        final boolean found = matcher.find();
        if (found) {
            return Optional.absent();
        }
        final int margin = configuration.getThreshold()
                                        .getValue();
        if (line.length() > margin) {
            final CodestyleViolation result = new CodestyleViolation(line);
            return Optional.of(result);
        }
        return Optional.absent();
    }

    private enum JavadocPattern {

        LINK(compile("import|<a href"));

        private final Pattern pattern;

        JavadocPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        Pattern getPattern() {
            return pattern;
        }
    }

    private static Logger log() {
        return RightMarginValidator.LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(RightMarginValidator.class);
    }

}
