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

package io.spine.tools.gradle;

import io.spine.annotation.Internal;

/**
 * A name of a Gradle task.
 *
 * @see BaseTaskName
 * @see JavaTaskName
 * @see ProtobufTaskName
 * @see ModelCompilerTaskName
 * @see JavadocPrettifierTaskName
 * @see ModelVerifierTaskName
 * @see ProtoJsTaskName
 * @see ProtoDartTaskName
 * @see DynamicTaskName
 */
@Internal
public interface TaskName {

    /**
     * The value of the name.
     *
     * <p>If an enum implements this interface, it is expected to name its constants so that
     * the {@link Enum#name()} obtains the name of the task.
     */
    String name();

    /**
     * Obtains this task name as a path.
     *
     * <p>It is expected that the referred task belongs to the root project (a.k.a {@code :}).
     *
     * @return the name with a colon symbol ({@code :}) at the beginning
     */
    default String path() {
        return ':' + name();
    }
}
