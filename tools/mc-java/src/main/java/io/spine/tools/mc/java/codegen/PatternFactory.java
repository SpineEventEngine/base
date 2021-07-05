/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.codegen;

import io.spine.tools.protoc.FilePattern;
import org.checkerframework.checker.regex.qual.Regex;

import static io.spine.tools.mc.java.codegen.FilePatterns.filePrefix;
import static io.spine.tools.mc.java.codegen.FilePatterns.fileRegex;
import static io.spine.tools.mc.java.codegen.FilePatterns.fileSuffix;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A factory of file patterns.
 */
public final class PatternFactory {

    private static final PatternFactory instance = new PatternFactory();

    static PatternFactory instance() {
        return instance;
    }

    /**
     * Prevents direct instantiation.
     */
    private PatternFactory() {
    }

    /**
     * Creates a new suffix pattern.
     *
     * @param value
     *         the suffix value
     */
    public FilePattern suffix(String value) {
        checkNotEmptyOrBlank(value);
        return fileSuffix(value);
    }

    /**
     * Creates a new prefix pattern.
     *
     * @param value
     *         the prefix value
     */
    public FilePattern prefix(String value) {
        checkNotEmptyOrBlank(value);
        return filePrefix(value);
    }

    /**
     * Creates a new regular expression pattern.
     *
     * @param value
     *         the regex value
     */
    public FilePattern regex(@Regex String value) {
        checkNotEmptyOrBlank(value);
        return fileRegex(value);
    }
}
