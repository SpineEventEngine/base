/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import java.net.URL;

/**
 * A utility class that provides references to descriptors.
 */
@SuppressWarnings("HardcodedLineSeparator")
// Resistance against different line separators is one of the things being tested.
public class DescriptorReferenceTestEnv {

    // Prevent instantiation.
    private DescriptorReferenceTestEnv() {
    }

    /** Returns a reference to a {@code "smoke-test-model-compiler.desc"} file. */
    public static ReferenceWithNewline toSmokeTestModelCompiler() {
        String reference = "smoke_tests_model-compiler_tests_unspecified.desc";
        return new ReferenceWithNewline(reference);
    }

    /** Returns a reference to a {@code "known_types.desc"} file. */
    public static ReferenceWithNewline toKnownTypes() {
        URL resource = Resources.getResource(FileDescriptors.KNOWN_TYPES);
        return new ReferenceWithNewline(resource.toString());
    }

    /**
     * A reference to a descriptor that contains a new line at its end.
     */
    public static class ReferenceWithNewline {

        private final String referencedFile;
        private static final String WINDOWS_SEPARATOR = "\r\n";
        private static final String UNIX_SEPARATOR = "\n";

        private ReferenceWithNewline(String file) {
            this.referencedFile = file;
        }

        /** Returns a reference to a descriptor with {@code "\r\n"} newline symbol at the end. */
        public DescriptorReference withCrLf() {
            String result = referencedFile + WINDOWS_SEPARATOR;
            return DescriptorReference.toOneFile(new File(result));
        }

        /** Returns a reference to a descriptor with {@code "\n"} newline symbol at the end. */
        public DescriptorReference withLf(){
            String result = referencedFile + UNIX_SEPARATOR;
            return DescriptorReference.toOneFile(new File(result));
        }
    }
}
