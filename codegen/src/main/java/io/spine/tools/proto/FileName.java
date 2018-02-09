/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.proto;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.type.StringTypeValue;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.CodePreconditions.checkNotEmptyOrBlank;

/**
 * A name of a Protobuf source code file.
 *
 * @author Alexander Yevsyukov
 */
public final class FileName extends StringTypeValue {

    /** Proto file names use underscores to separate words. */
    private static final String WORD_SEPARATOR = "_";

    /** The standard file extension. */
    private static final String EXTENSION = ".proto";

    /** The file system separator as defined by Protobuf. Not platform-dependant. */
    private static final char PATH_SEPARATOR = '/';

    private FileName(String value) {
        super(value);
    }

    /**
     * Creates new proto file name with the passed value.
     */
    public static FileName of(String value) {
        checkNotEmptyOrBlank(value);
        return new FileName(value);
    }

    /**
     * Obtains immutable list of words used in the name of the file.
     */
    public List<String> words() {
        final String[] words = nameOnly().split(WORD_SEPARATOR);
        final ImmutableList<String> result = ImmutableList.copyOf(words);
        return result;
    }

    /**
     * Obtains the file name from the passed descriptor.
     */
    public static FileName from(FileDescriptorProto descr) {
        checkNotNull(descr);
        final FileName result = of(descr.getName());
        return result;
    }

    /**
     * Obtains the file name without path and extension.
     */
    public String nameOnly() {
        final String value = value();
        final int lastBackslashIndex = value.lastIndexOf(PATH_SEPARATOR);
        final int extensionIndex = value.lastIndexOf(EXTENSION);
        final String result = value.substring(lastBackslashIndex + 1, extensionIndex);
        return result;
    }

    /**
     * Returns the file name without path and extension in the {@code CamelCase}.
     */
    public String nameOnlyCamelCase() {
        final StringBuilder result = new StringBuilder(value().length());
        for (final String word : words()) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                result.append(word.substring(1)
                                  .toLowerCase());
            }
        }
        return result.toString();
    }
}
