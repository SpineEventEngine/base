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
package io.spine.tools.codestyle.javadoc;

import io.spine.tools.codestyle.CodeStyleViolation;
import io.spine.tools.codestyle.LineStorage;

import java.nio.file.Path;
import java.util.Map;

import static java.lang.String.format;

/**
 * Utility class to save and address results of fully qualified name javadoc validate.
 *
 * @author Alexander Aleksandrov
 */
class InvalidResultStorage extends LineStorage {

    @Override
    public void logViolations() {
        for (Map.Entry<Path, CodeStyleViolation> entry : entries()) {
            log(entry);
        }
    }

    private void log(Map.Entry<Path, CodeStyleViolation> entry) {
        final CodeStyleViolation v = entry.getValue();
        final Path file = entry.getKey();
        final String msg = format(
                " Wrong link format found: %s on %s line in %s",
                v.getCodeLine(), v.getLineNumber(), file
        );
        log().error(msg);
    }
}
