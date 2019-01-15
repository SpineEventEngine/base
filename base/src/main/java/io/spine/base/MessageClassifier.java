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

package io.spine.base;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.annotation.Internal;

import java.util.function.BiPredicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Evaluates the message Proto definition against some contract.
 */
@Internal
@Immutable
public abstract class MessageClassifier
        implements BiPredicate<DescriptorProto, FileDescriptorProto> {

    /**
     * Checks if the given message definition matches this classifier's contract.
     *
     * <p>Message's declaring file is also taken into account.
     *
     * @param message
     *         the message definition to check
     * @param declaringFile
     *         the message's declaring file
     * @return {@code true} if the message matches the contract, {@code false} otherwise
     * @throws IllegalArgumentException
     *         if the given file does not contain the given message
     */
    @Override
    public boolean test(DescriptorProto message, FileDescriptorProto declaringFile) {
        checkNotNull(message);
        checkNotNull(declaringFile);
        boolean messageInFile = declaringFile.getMessageTypeList()
                                             .contains(message);
        checkArgument(messageInFile,
                      "The passed file %s does not contain the specified message type %s",
                      declaringFile.getName(),
                      message.getName());
        return doTest(message, declaringFile);
    }

    protected abstract boolean doTest(DescriptorProto message, FileDescriptorProto declaringFile);
}
