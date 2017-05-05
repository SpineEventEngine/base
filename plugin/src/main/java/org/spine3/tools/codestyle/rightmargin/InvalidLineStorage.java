/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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
package org.spine3.tools.codestyle.rightmargin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.tools.codestyle.AbstractStorage;
import org.spine3.tools.codestyle.CodeStyleViolation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * A utility class to save and address results of the right margin wrapping validation.
 *
 * @author Alexander Aleksandrov
 */
public class InvalidLineStorage extends AbstractStorage {

    @Override
    public void logViolations() {
        for (Map.Entry<Path, List<CodeStyleViolation>> entry : getContent().entries()) {
            logInvalidLines(entry);
        }
    }

    private static void logInvalidLines(Map.Entry<Path, List<CodeStyleViolation>> entry) {
        for (CodeStyleViolation codeStyleViolation : entry.getValue()) {
            final String msg = format(
                    "Right margin trespassing found on line %s in %s",
                    codeStyleViolation.getIndex(),
                    entry.getKey());
            log().error(msg);
        }
    }

    private static Logger log() {
        return InvalidLineStorage.LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(InvalidLineStorage.class);
    }

}
