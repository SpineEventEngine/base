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
package org.spine3.tools.codestyle;

import java.nio.file.Path;
import java.util.List;

/**
 * An interface to gather all common operations for validators.
 *
 * @author Alexander Aleksandrov
 */
public interface CodeStyleFileValidator {

    /**
     * Validates the file on code style violations depending on implementation.
     *
     * @param path Path to the target file
     * @throws CodeStyleException
     */
    void validate(Path path) throws CodeStyleException;

    /**
     * Goes through the file content represented as list of strings.
     *
     * @param list Content of the file under validation.
     * @return List of {@link CodeStyleViolation} from that file.
     */
    List<CodeStyleViolation> checkForViolations(List<String> list);

    /**
     * Check the threshold parameter from build file.
     */
    void checkThreshold();

    /**
     * Describes the behavior in case if threshold is exceeded.
     */
    void onAboveThreshold();
}
