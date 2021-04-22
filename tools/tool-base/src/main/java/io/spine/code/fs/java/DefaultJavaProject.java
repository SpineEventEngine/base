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

package io.spine.code.fs.java;

import io.spine.code.fs.DefaultProject;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A default directory structure for a Spine-based Java project.
 *
 * <p>The project structure is based on the standard Maven/Gradle project conventions, with the
 * following directories under the project root:
 *
 * <ul>
 * <li>{@code src/main} — manually written code production code, with {@code java} and
 * {@code proto} sub-directories.
 *
 * <li>{@code src/test} — the code of tests.
 *
 * <li>{@code generated} — computer-generated code, with sub-directories:
 * <ul>
 *     <li>{@code java} — the code generated by the Protobuf Compiler.
 *     <li>{@code gRPC} — the code generated by the gPRC Protobuf Compiler Plug-in.
 *     <li>{@code spine} — the code generated by the Spine Model Compiler.
 * </ul>
 * </li>
 *
 * <li>{@code .spine} — temporary build artifacts directory used by the Spine Model Compiler.
 * </ul>
 */
public final class DefaultJavaProject extends DefaultProject {

    private DefaultJavaProject(Path path) {
        super(path);
    }

    public static DefaultJavaProject at(Path root) {
        checkNotNull(root);
        DefaultJavaProject result = new DefaultJavaProject(root);
        return result;
    }

    public static DefaultJavaProject at(File projectDir) {
        checkNotNull(projectDir);
        return at(projectDir.toPath());
    }

    /**
     * Obtains the {@code "src"} directory with the handcrafted code.
     */
    public Handcrafted src() {
        return new Handcrafted(this);
    }

    /**
     * Obtains the directory with the {@code "generated"} code.
     */
    public Generated generated() {
        return new Generated(this);
    }
}
