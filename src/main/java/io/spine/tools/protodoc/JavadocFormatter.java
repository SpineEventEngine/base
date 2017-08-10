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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.spine.tools.protodoc.JavaSources.isJavaFile;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A formatter for Javadocs.
 *
 * <p>The formatter executes {@linkplain FormattingAction formatting actions}
 * for the Javadoc lines in a source file.
 *
 * @author Dmytro Grankin
 */
class JavadocFormatter {

    /**
     * The formatting actions to perform.
     */
    private final List<FormattingAction> actions;

    JavadocFormatter(List<FormattingAction> actions) {
        this.actions = actions;
    }

    /**
     * Formats the Javadocs in the file with the specified path.
     *
     * <p>If the file is not a {@code .java} source, does noting.
     *
     * @param path the path to the file
     */
    void format(Path path) {
        if (!isJavaFile(path)) {
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
            Files.write(path, updatedContent, UTF_8);
        } catch (IOException e) {
            final String msg = String.format("Cannot write the content to the file `%s`.", path);
            throw new IllegalStateException(msg, e);
        }
    }

    private static List<String> getFileContent(Path path) {
        try {
            return Files.readAllLines(path, UTF_8);
        } catch (IOException e) {
            final String msg = String.format("Cannot read the content of the file `%s`.", path);
            throw new IllegalStateException(msg, e);
        }
    }
}
