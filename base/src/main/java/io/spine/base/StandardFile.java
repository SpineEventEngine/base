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

package io.spine.base;

import io.spine.annotation.Internal;
import io.spine.base.MessageFile.Predicate;

/**
 * A enumeration of standard Proto files for events, commands, etc.
 *
 * <p>File names reflect the Spine naming conventions for the given file types.
 */
@Internal
public enum StandardFile {

    EVENTS_FILE("events"),
    COMMANDS_FILE("commands"),
    REJECTIONS_FILE("rejections");

    private final MessageFile file;

    StandardFile(String suffix) {
        file = new MessageFile(suffix) {
            private static final long serialVersionUID = 0L;
        };
    }

    /**
     * Provides the predicate for finding proto files with the appropriate message declarations.
     */
    public Predicate predicate() {
        return file.predicate();
    }

    /**
     * Provides a suffix by which such file can be located.
     */
    public String suffix() {
        return file.value();
    }
}
