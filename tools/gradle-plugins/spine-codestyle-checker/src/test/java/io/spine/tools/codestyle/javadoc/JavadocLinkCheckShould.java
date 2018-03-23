/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.codestyle.javadoc;

import io.spine.tools.codestyle.Given;
import io.spine.tools.codestyle.ReportType;
import io.spine.tools.codestyle.StepConfiguration;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Alexander Aleksandrov
 */
public class JavadocLinkCheckShould {

    private JavadocLinkCheck check;
    private static final String MULTIPLE_WRONG_FQN_LINKS_JAVA = "MultipleWrongFqnLinks.java";

    private static final String warnReportType = ReportType.WARN.getValue();
    private static final String errorReportType = ReportType.ERROR.getValue();

    @Test(expected = RuntimeException.class)
    public void throw_exception_for_invalid_fqn_links_over_threshold() throws InvalidJavadocLinkException {
        check = setUpValidator(2, errorReportType);
        check.process(getPath(MULTIPLE_WRONG_FQN_LINKS_JAVA));
    }

    @Test(expected = RuntimeException.class)
    public void throw_exception_file_format_is_not_utf8() throws InvalidJavadocLinkException {
        check = setUpValidator(2, errorReportType);
        check.process(getPath(MULTIPLE_WRONG_FQN_LINKS_JAVA));
    }

    @Test
    public void check_only_files_with_java_extension() {
        JavadocLinkCheck mockedObject = mock(JavadocLinkCheck.class);
        final Path path = getPath(".hidden_file");
        final List<String> list = new ArrayList<>();
        mockedObject.process(path);
        verify(mockedObject).process(path);
        verify(mockedObject, never()).findViolations(list);
    }

    @Test
    public void throw_warning_for_invalid_fqn_links_over_threshold() {
        JavadocLinkCheck impl = spy(
                new JavadocLinkCheck(setStepConfiguration(0, warnReportType)));
        final Path path = getPath(MULTIPLE_WRONG_FQN_LINKS_JAVA);
        impl.process(path);
        verify(impl).process(path);
    }

    @Test
    public void allow_correct_fqn_links() throws RuntimeException {
        JavadocLinkCheck impl = spy(
                new JavadocLinkCheck(setStepConfiguration(0, warnReportType)));
        final Path path = getPath(Given.testFile());
        impl.process(path);
        verify(impl).process(path);
    }

    private static JavadocLinkCheck setUpValidator(int threshold, String reportType) {
        JavadocLinkCheck validator = new JavadocLinkCheck(
                setStepConfiguration(threshold, reportType));
        return validator;
    }

    private static StepConfiguration setStepConfiguration(int threshold, String reportType) {
        final StepConfiguration configuration = new StepConfiguration();
        configuration.setThreshold(threshold);
        configuration.setReportType(reportType);
        return configuration;
    }

    private Path getPath(String fileName) {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL resource = classLoader.getResource(fileName);
        checkNotNull(resource);
        final String pathname = resource.getFile();
        final File file = new File(pathname);
        final Path path = Paths.get(file.getAbsolutePath());
        return path;
    }
}
