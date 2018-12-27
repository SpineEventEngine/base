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

package io.spine.code.java;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Deque;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.code.java.ClassName.OUTER_CLASS_DELIMITER;

/**
 * Utilities for working with names of proto types.
 */
final class Names {

    /** Prevents instantiation of this utility class. */
    private Names() {
    }

    /**
     * Obtains outer class prefix, if the file has {@code java_multiple_files} set to {@code false}.
     * If the option is set, returns an empty string.
     */
    static String outerClassPrefix(FileDescriptor file) {
        checkNotNull(file);
        boolean multipleFiles = file.getOptions()
                                    .getJavaMultipleFiles();
        if (multipleFiles) {
            return "";
        } else {
            String className = SimpleClassName.outerOf(file)
                                              .value();
            return className + OUTER_CLASS_DELIMITER;
        }
    }

    /**
     * Obtains prefix for a type which is enclosed into the passed message.
     * If null value is passed, returns an empty string.
     */
    static String containingClassPrefix(@Nullable Descriptor containingMessage) {
        if (containingMessage == null) {
            return "";
        }
        Deque<String> parentClassNames = newLinkedList();
        Descriptor current = containingMessage;
        while (current != null) {
            parentClassNames.addFirst(current.getName() + OUTER_CLASS_DELIMITER);
            current = current.getContainingType();
        }
        String result = String.join("", parentClassNames);
        return result;
    }
}
