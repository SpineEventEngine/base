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

package io.spine.tools.protoc.plugin.message;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileOption;
import io.spine.option.IsOption;
import io.spine.option.OptionsProto;
import io.spine.type.MessageType;

import java.util.Optional;

/**
 * An option for a specified file which defines if marker interfaces should be generated and
 * the Java type of the message.
 *
 * @see Is Is option
 */
final class EveryIs extends FileOption<IsOption> {

    /** Creates a new instance of this option. */
    private EveryIs() {
        super(OptionsProto.everyIs);
    }

    /**
     * Obtains a value of the option declared for every type declared in the same file with
     * the passed message type.
     *
     * @return the value of the option, or {@code Optional.empty()} if the option is not specified
     */
    static Optional<IsOption> of(MessageType type) {
        FileDescriptor file = type.file();
        Optional<IsOption> value = new EveryIs().valueFrom(file);
        return value;
    }
}
