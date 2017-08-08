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
package io.spine.tools.protodoc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract class to gather not public common methods for validators.
 *
 * @author Alexander Aleksandrov
 */
public abstract class AbstractJavadocFileFormatter implements JavadocFileFormatter {

    private static final String READ_FILE_ERR_MSG = "Cannot read the contents of the file: ";
    private static final String WRITE_FILE_ERR_MSG = "Cannot write the contents to the file: ";

    AbstractJavadocFileFormatter() {
    }

    @Override
    public void format(Path path) {
        final List<String> content;
        if (!JavaSources.isJavaFile(path)) {
            return;
        }

        try {
            content = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(READ_FILE_ERR_MSG + path, e);
        }

        final List<String> updatedContent = checkForCases(content);
        try {
            Files.write(path, updatedContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(WRITE_FILE_ERR_MSG + path, e);
        }
    }

    /**
     * Goes through the file content represented as list of strings.
     *
     * @param list Content of the file under validation.
     */
    protected abstract List<String> checkForCases(List<String> list);
}
