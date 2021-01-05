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

package io.spine.code.proto.given;

import com.google.common.io.Resources;
import io.spine.code.proto.DescriptorReference;
import io.spine.code.proto.FileDescriptors;

import java.io.File;
import java.util.UUID;

/**
 * A utility class that provides references to descriptors.
 */
public class DescriptorReferenceTestEnv {

    // Prevent instantiation.
    private DescriptorReferenceTestEnv() {
    }

    /** Returns a reference to a {@code "smoke-test-model-compiler.desc"} file. */
    public static DescriptorReference smokeTestModelCompilerRef() {
        String reference = "smoke_tests_model-compiler_tests_unspecified.desc";
        return DescriptorReference.toOneFile(new File(reference));
    }

    /** Returns a reference to a {@code "known_types.desc"} file. */
    public static DescriptorReference knownTypesRef() {
        String asFile = Resources.getResource(FileDescriptors.KNOWN_TYPES)
                                 .getFile();
        File result = new File(asFile);
        return DescriptorReference.toOneFile(result);
    }

    /**
     * Return a reference to a descriptor file with a random name. Note that returned file does not
     * exist.
     */
    public static DescriptorReference randomRef() {
        String reference = UUID.randomUUID()
                               .toString();
        File result = new File(reference);
        return DescriptorReference.toOneFile(result);
    }
}
