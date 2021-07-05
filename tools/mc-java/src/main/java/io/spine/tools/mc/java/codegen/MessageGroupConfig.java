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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.tools.gradle.Multiple;
import io.spine.tools.protoc.FilePattern;
import org.gradle.api.Project;

import java.util.Set;

import static io.spine.protobuf.Messages.isDefault;

/**
 * A configuration for code generation for a certain group of messages joined by a file pattern.
 *
 * @param <P>
 *         Protobuf type reflecting a snapshot of this configuration
 */
abstract class MessageGroupConfig<P extends Message> extends ConfigWithFields<P> {

    private final Multiple<FilePattern> file;

    MessageGroupConfig(Project p) {
        super(p);
        this.file = new Multiple<>(p, FilePattern.class);
    }

    /**
     * Sets up the default value for the file pattern.
     *
     * @param pattern
     *         the default value for the pattern
     */
    void convention(FilePattern pattern) {
        ImmutableSet<FilePattern> defaultValue;
        if (isDefault(pattern)) {
            defaultValue = ImmutableSet.of();
        } else {
            defaultValue = ImmutableSet.of(pattern);
        }
        file.convention(defaultValue);
    }

    /**
     * Obtains the Gradle set property with the file pattern which match messages in this group.
     */
    final Set<FilePattern> patterns() {
        return file.get();
    }

    /**
     * Specifies a file pattern for this group of messages.
     *
     * <p>Calling this method many times will extend the group to include more types. If a type is
     * declared in a file which matches al least one of the patterns, the type is included in
     * the group.
     *
     * <p>In the example below, all messages declared in files which either end with "ids.proto" or
     * contain the word "identifiers" will be included into the group.
     *
     * <pre>
     *     includeFiles(by().suffix("ids.proto"))
     *     includeFiles(by().regex(".*identifiers.*"))
     * </pre>
     *
     * @see #by() for creating file patterns
     */
    public void includeFiles(FilePattern pattern) {
        this.file.add(pattern);
    }

    /**
     * Obtains a factory of file patterns for selecting Protobuf files.
     *
     * @see #includeFiles
     */
    public PatternFactory by() {
        return PatternFactory.instance();
    }
}
