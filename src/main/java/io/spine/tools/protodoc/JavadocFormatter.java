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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Dmytro Grankin
 */
class JavadocFormatter {

    private static final String READ_FILE_ERR_MSG = "Cannot read the contents of the file: ";
    private static final String WRITE_FILE_ERR_MSG = "Cannot write the contents to the file: ";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final List<FormattingAction> actions;

    JavadocFormatter(List<FormattingAction> actions) {
        this.actions = actions;
    }

    void format(Path path) {
        if (!JavaSources.isJavaFile(path)) {
            return;
        }

        final List<String> content = getFileContent(path);
        final List<String> formattedContentContent = formatFileContent(content);
        writeFile(path, formattedContentContent);
    }

    private List<String> formatFileContent(List<String> lines) {
        List<String> currentState = lines;
        for (FormattingAction action : actions) {
            currentState = action.execute(currentState);
        }
        return currentState;
    }

    private static void writeFile(Path path, Iterable<String> updatedContent) {
        try {
            Files.write(path, updatedContent, CHARSET);
        } catch (IOException e) {
            throw new IllegalStateException(WRITE_FILE_ERR_MSG + path, e);
        }
    }

    private static List<String> getFileContent(Path path) {
        try {
            return Files.readAllLines(path, CHARSET);
        } catch (IOException e) {
            throw new IllegalStateException(READ_FILE_ERR_MSG + path, e);
        }
    }
}
