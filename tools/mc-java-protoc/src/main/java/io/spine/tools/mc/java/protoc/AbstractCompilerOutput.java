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

package io.spine.tools.mc.java.protoc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

/**
 * An implementation base for a Protobuf compiler output items.
 */
public abstract class AbstractCompilerOutput implements CompilerOutput {

    private final File file;

    /**
     * Creates a new {@code AbstractCompilerOutput} with the given {@link File}.
     *
     * @param file
     *         the {@linkplain #asFile() file representation} of this compiler output item
     */
    protected AbstractCompilerOutput(File file) {
        this.file = file;
    }

    @Override
    public final File asFile() {
        return file;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractCompilerOutput)) {
            return false;
        }
        AbstractCompilerOutput output = (AbstractCompilerOutput) o;
        return Objects.equal(file, output.file);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(file);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("file", file)
                          .toString();
    }
}
