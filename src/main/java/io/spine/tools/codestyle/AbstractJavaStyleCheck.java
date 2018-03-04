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
package io.spine.tools.codestyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract base for code style checks.
 *
 * @author Alexander Aleksandrov
 * @author Alexander Yevsyukov
 */
public abstract class AbstractJavaStyleCheck implements CodeStyleCheck {

    /**
     * Violations found in the currently processed file.
     *
     * <p>Is {@code null} before or after a file is processed.
     */
    @Nullable
    private FileViolations violations;

    protected AbstractJavaStyleCheck() {
    }

    /**
     * Checks the file path.
     *
     * @param path  the target file path
     * @return {@code true} in case if the file has the .java extension.
     */
    private static boolean isJavaFile(Path path) {
        return path.toString()
                   .endsWith(".java");
    }

    protected int numberOfViolations() {
        if (violations != null) {
            return violations.size();
        }
        return 0;
    }

    protected void reportViolations() {
        if (violations != null) {
            violations.reportViolations(this);
        }
    }

    @Override
    public void process(Path file) throws CodeStyleException {
        if (!isJavaFile(file)) {
            return;
        }

        this.violations = new FileViolations(file);
        final List<String> content = loadFile(file);
        final List<CodeStyleViolation> found = findViolations(content);
        this.violations.save(found);
        processResult();
        this.violations = null;
    }

    private static List<String> loadFile(Path file) {
        final List<String> content;
        try {
            content = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the contents of the file: " + file, e);
        }
        return content;
    }

    /**
     * Goes through the file content represented as list of strings.
     *
     * @param fileContent Content of the file under validation.
     * @return List of {@link CodeStyleViolation} from that file.
     */
    protected abstract List<CodeStyleViolation> findViolations(List<String> fileContent);

    /**
     * Processes the found violations.
     */
    protected abstract void processResult();

    /**
     * Obtains class-specific logger instance.
     */
    protected Logger log() {
        return LoggerFactory.getLogger(getClass());
    }
}
