/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Alexander Yevsyukov
 */
public class Rejections {

    /** Prevents instantiation of this utility class. */
    private Rejections() {
    }

    public static List<RejectionsFile> collect(Iterable<FileDescriptorProto> files) {
        final List<RejectionsFile> result = Lists.newLinkedList();
        final Logger log = log();
        for (FileDescriptorProto file : files) {
            final FileName fn = FileName.from(file);
            if (fn.isRejections()) {
                log.trace("Found rejections file: {}", fn.value());

                // See if the file content matches conventions.
                final SourceFile sourceFile = SourceFile.from(file);
                if (sourceFile.isRejections()) {
                    final RejectionsFile rejectionsFile = RejectionsFile.from(sourceFile);
                    result.add(rejectionsFile);
                } else {
                    log.error("Invalid rejections file: {}", file.getName());
                }
            }
        }
        log.trace("Found rejections in files: {}", result);

        return result;
    }

    private enum LogSingleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(Rejections.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
