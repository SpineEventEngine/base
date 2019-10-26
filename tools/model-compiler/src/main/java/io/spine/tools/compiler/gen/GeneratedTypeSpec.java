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

package io.spine.tools.compiler.gen;

import com.squareup.javapoet.TypeSpec;
import io.spine.code.gen.Indent;
import io.spine.code.java.PackageName;

import java.nio.file.Path;

/**
 * A JavaPoet-based spec of a generated type.
 */
public interface GeneratedTypeSpec {

    /**
     * The package under which the type will be generated.
     */
    PackageName packageName();

    /**
     * A JavaPoet spec of the type.
     */
    TypeSpec typeSpec();

    /**
     * Writes the generated type to a file.
     *
     * @param targetDir
     *         the root dir to write to
     * @param indent
     *         the indent to use
     */
    default void writeToFile(Path targetDir, Indent indent) {
        Writer writer = new Writer(this);
        writer.setIndent(indent);
        writer.write(targetDir);
    }
}
