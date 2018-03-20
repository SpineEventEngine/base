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

import com.google.common.base.MoreObjects;
import io.spine.util.Preconditions2;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Provides information a code style violation with the code line and its number.
 *
 * @author Alexander Aleksandrov
 * @author Alexander Yevsyukov
 */
public class CodeStyleViolation {

    private static final int LINE_NUMBER_UNKNOWN = -1;

    private final String codeLine;
    private final int lineNumber;

    public CodeStyleViolation(String codeLine) {
        this.codeLine = Preconditions2.checkNotEmptyOrBlank(codeLine);
        this.lineNumber = LINE_NUMBER_UNKNOWN;
    }

    private CodeStyleViolation(String codeLine, int lineNumber) {
        Preconditions2.checkNotEmptyOrBlank(codeLine);
        checkArgument(lineNumber >= 0, "Line number must be non-negative");

        this.codeLine = codeLine;
        this.lineNumber = lineNumber;
    }

    /**
     * Obtains the line of code with the violation.
     */
    public String getCodeLine() {
        return codeLine;
    }

    /**
     * Obtains the line number.
     *
     * @return a non-negative value or -1 if the line number is not available.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Creates a new instance with the same code line value and the passed line number.
     */
    public CodeStyleViolation withLineNumber(int lineNumber) {
        final CodeStyleViolation result = new CodeStyleViolation(this.codeLine, lineNumber);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("codeLine", codeLine)
                          .add("lineNumber", lineNumber)
                          .toString();
    }
}
