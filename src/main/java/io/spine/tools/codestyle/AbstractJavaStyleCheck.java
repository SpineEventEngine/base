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

import io.spine.tools.codestyle.rightmargin.InvalidLineLengthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Abstract base for code style checks.
 *
 * @author Alexander Aleksandrov
 * @author Alexander Yevsyukov
 */
public abstract class AbstractJavaStyleCheck implements CodeStyleCheck {

    private final LineStorage storage;

    protected AbstractJavaStyleCheck() {
        this.storage = new LineStorage();
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
        return storage.size();
    }

    protected void reportViolations() {
        storage.reportViolations(this);
    }

    @Override
    public void process(Path file) throws InvalidLineLengthException {
        final List<String> content;
        if (!isJavaFile(file)) {
            return;
        }
        try {
            content = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the contents of the file: " + file, e);
        }
        final List<CodeStyleViolation> violations = findViolations(content);
        save(file, violations);
        processResult();
        storage.clear();
    }

    private void save(Path file, Collection<CodeStyleViolation> violations) {
        if (!violations.isEmpty()) {
            storage.save(file, violations);
        }
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
