/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protojs.code;

import java.util.List;

import static java.lang.String.join;
import static java.util.Collections.nCopies;

/**
 * The JS code line.
 *
 * <p>Consists of the code itself and the depth on which the code is located.
 *
 * <p>For example, the code inside the {@code if} block is one unit deeper than the {@code if}
 * condition itself.
 *
 * @author Dmytro Kuzmin
 */
final class CodeLine {

    private static final String SPACE = " ";

    private final String content;
    private final int depth;

    /**
     * Creates a new {@code CodeLine}.
     *
     * @param content
     *         the JS code
     * @param depth
     *         the depth of the code
     */
    CodeLine(String content, int depth) {
        this.content = content;
        this.depth = depth;
    }

    /**
     * Prints the {@code CodeLine} to the {@code String} with the specified indentation.
     *
     * @param indentation
     *         the indentation of the file where this line will be written
     * @return the {@code CodeLine} content with the correct indentation
     */
    String printToString(int indentation) {
        int indentUnits = depth * indentation;
        List<String> spaces = nCopies(indentUnits, SPACE);
        String indent = join("", spaces);
        String result = indent + content;
        return result;
    }
}
