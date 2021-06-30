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
import io.spine.tools.protoc.FilePattern;
import org.gradle.api.Project;
import org.gradle.api.provider.SetProperty;

import java.util.Set;

/**
 * A configuration for code generation for a certain group of messages joined by a file pattern.
 *
 * @param <P>
 *         Protobuf type which serializes this configuration
 */
abstract class MessageGroupConfig<P extends Message> extends ConfigWithFields<P> {

    private final SetProperty<FilePattern> file;

    MessageGroupConfig(Project p) {
        super(p);
        this.file = p.getObjects()
                     .setProperty(FilePattern.class);
    }

    void convention(FilePattern pattern) {
        file.convention(ImmutableSet.of(pattern));
    }

    Set<FilePattern> patterns() {
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
    public void includeFiles(ByPattern pattern) {
        this.file.add(pattern.toProto());
    }

    /**
     * Obtains a factory of file patterns for selecting Protobuf files.
     *
     * @see #includeFiles(ByPattern)
     */
    public MessageSelectorFactory by() {
        return MessageSelectorFactory.INSTANCE;
    }
}
