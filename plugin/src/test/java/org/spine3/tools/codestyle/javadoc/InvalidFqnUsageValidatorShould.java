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
import org.spine3.tools.codestyle.rightmargin.RightMarginValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class InvalidFqnUsageValidatorShould {
    public InvalidFqnUsageValidator validator;

    @Test(expected = RuntimeException.class)
    public void throw_exception_for_invalid_fqn_links_over_threshold() throws InvalidFqnUsageException {
        validator = setUpValidator(2, "error");
        validator.validate(getPath("MultipleWrongFqnLinks.java"));
    }

    @Test(expected = RuntimeException.class)
    public void throw_exception_file_format_is_not_utf8() throws InvalidFqnUsageException {
        validator = setUpValidator(2, "error");
        validator.validate(getPath("MultipleWrongFqnLinks.java"));
    }

    @Test
    public void check_only_files_with_java_extension() {
        validator = setUpValidator(0, "error");
        validator.validate(getPath(".hiden_file"));
    }

    @Test
    public void throw_warning_for_invalid_fqn_links_over_threshold() {
        validator = setUpValidator(2, "warn");
        validator.validate(getPath("MultipleWrongFqnLinks.java"));
    }

    @Test
    public void allow_correct_fqn_links() throws IOException {
        validator = setUpValidator(0, "error");
        validator.validate(getPath("AllowedFqnFormats.java"));

    }

    private static InvalidFqnUsageValidator setUpValidator(int threshold, String reportType) {
        final StepConfiguration configuration = new StepConfiguration();
        configuration.setThreshold(threshold);
        configuration.setReportType(reportType);
        InvalidFqnUsageValidator validator = new InvalidFqnUsageValidator(configuration);
        return validator;
    }

    private Path getPath (String fileName){
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(fileName).getFile());
        final Path path = Paths.get(file.getAbsolutePath());
        return path;
    }
}
