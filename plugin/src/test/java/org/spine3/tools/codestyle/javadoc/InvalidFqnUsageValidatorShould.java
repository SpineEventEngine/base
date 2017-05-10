/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import org.junit.Test;
import org.spine3.tools.codestyle.StepConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.spine3.tools.codestyle.Given.*;

public class InvalidFqnUsageValidatorShould {

    private InvalidFqnUsageValidator validator;
    private static final String MULTIPLE_WRONG_FQN_LINKS_JAVA = "MultipleWrongFqnLinks.java";
    private static final String errorReportType = "error";
    private static final String warnReportType = "warn";

    @Test(expected = RuntimeException.class)
    public void throw_exception_for_invalid_fqn_links_over_threshold() throws InvalidFqnUsageException {
        validator = setUpValidator(2, errorReportType);
        validator.validate(getPath(MULTIPLE_WRONG_FQN_LINKS_JAVA));
    }

    @Test(expected = RuntimeException.class)
    public void throw_exception_file_format_is_not_utf8() throws InvalidFqnUsageException {
        validator = setUpValidator(2, errorReportType);
        validator.validate(getPath(MULTIPLE_WRONG_FQN_LINKS_JAVA));
    }

    @Test
    public void check_only_files_with_java_extension() {
        InvalidFqnUsageValidator mockedObject = mock(InvalidFqnUsageValidator.class);
        final Path path = getPath(".hiden_file");
        final List<String> list = new ArrayList<>();
        mockedObject.validate(path);
        verify(mockedObject).validate(path);
        verify(mockedObject, never()).checkForViolations(list);
    }

    @Test
    public void throw_warning_for_invalid_fqn_links_over_threshold() {
        InvalidFqnUsageValidator impl = spy(
                new InvalidFqnUsageValidator(setStepConfiguration(0, warnReportType)));
        final Path path = getPath(MULTIPLE_WRONG_FQN_LINKS_JAVA);
        impl.validate(path);
        verify(impl).validate(path);
    }

    @Test
    public void allow_correct_fqn_links() throws RuntimeException {
        InvalidFqnUsageValidator impl = spy(
                new InvalidFqnUsageValidator(setStepConfiguration(0, warnReportType)));
        final Path path = getPath(testFile());
        impl.validate(path);
        verify(impl).validate(path);
    }

    private static InvalidFqnUsageValidator setUpValidator(int threshold, String reportType) {
        InvalidFqnUsageValidator validator = new InvalidFqnUsageValidator(
                setStepConfiguration(threshold, reportType));
        return validator;
    }

    private static StepConfiguration setStepConfiguration(int threshold, String reportType) {
        final StepConfiguration configuration = new StepConfiguration();
        configuration.setThreshold(threshold);
        configuration.setReportType(reportType);
        return configuration;
    }

    private Path getPath (String fileName){
        final ClassLoader classLoader = getClass().getClassLoader();
        final String pathname = classLoader.getResource(fileName)
                                        .getFile();
        final File file = new File(pathname);
        final Path path = Paths.get(file.getAbsolutePath());
        return path;
    }
}
