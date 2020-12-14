/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests resolving of {@link com.google.protobuf.Descriptors.FileDescriptor FileDescriptor}s.
 */
@DisplayName("Linker should")
class LinkerTest {

    private Linker linker;

    @BeforeEach
    void setUp() throws DescriptorValidationException {
        Collection<FileDescriptorProto> fileSets = FileDescriptors.load();
        linker = new Linker(fileSets);
        linker.resolve();
    }

    @Test
    @DisplayName("resolve files")
    void resolveFiles() {
        FileSet resolved = linker.resolved();
        assertTrue(resolved.size() > 0);
        assertTrue(resolved.containsAll(ImmutableList.of(
                FileName.of("google/protobuf/any.proto"),
                FileName.of("google/protobuf/descriptor.proto")
        )));
    }

    @Test
    @DisplayName("obtain partially resolved files")
    void obtainPartial() {
        // No such in the given test data.
        assertTrue(linker.partiallyResolved()
                         .isEmpty());
    }

    @Test
    @DisplayName("obtain unresolved files")
    void obtainUnresolved() {
        // No such in the given test data.
        assertTrue(linker.unresolved()
                         .isEmpty());
    }

    @Test
    @DisplayName("not leave remaining")
    void doNotLeaveRemaining() {
        assertTrue(linker.remaining()
                         .isEmpty());
    }
}
