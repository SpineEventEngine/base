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

package io.spine.js.generate.resolve;

import com.google.common.collect.ImmutableList;
import io.spine.code.js.ImportPath;

import java.util.List;
import java.util.Objects;

/**
 * A JavaScript module to resolve in compiled Protobuf files.
 */
public final class ResolvableModule {

    private final String name;
    private final List<PackagePattern> packages;

    /**
     * Creates a new instance.
     *
     * @param name
     *         the name of the module to be resolved
     * @param packages
     *         patterns of packages provided by the resolvable module
     */
    public ResolvableModule(String name, List<PackagePattern> packages) {
        this.name = name;
        this.packages = ImmutableList.copyOf(packages);
    }

    /**
     * Tries to resolve the import within this module.
     */
    ImportSnippet resolve(ImportSnippet resolvable) {
        ImportPath importPath = resolvable.path();
        boolean provides = provides(importPath);
        if (!provides) {
            return resolvable;
        }
        String resolvedPath = name + ImportPath.separator() + importPath.skipRelativePath();
        return resolvable.replacePath(resolvedPath);
    }

    /**
     * Checks if the imported file is provided by the module.
     *
     * @param importPath
     *         the import path to check
     * @return {@code true} if the module provides the imported file
     */
    boolean provides(ImportPath importPath) {
        for (PackagePattern packagePattern : packages) {
            if (packagePattern.matches(importPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolvableModule)) {
            return false;
        }
        ResolvableModule module = (ResolvableModule) o;
        return name.equals(module.name) &&
                packages.equals(module.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, packages);
    }
}
