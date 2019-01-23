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
import io.spine.code.proto.PackageName;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * An external JavaScript module used in a project.
 *
 * <p>External means that it is provided by an artifact repository like NPM.
 */
public final class ExternalModule {

    private final String name;
    //TODO:2019-01-23:dmytro.grankin: supply the value from outside
    private final String protoDirectory = "proto";
    private final List<PackagePattern> packages;

    /**
     * Creates a new instance.
     *
     * @param name
     *         the name of the module, e.g. {@code library/proto}
     * @param packages
     *         patterns of packages provided by the module
     */
    public ExternalModule(String name, List<PackagePattern> packages) {
        this.name = checkNotEmptyOrBlank(name);
        this.packages = ImmutableList.copyOf(packages);
    }

    /**
     * Obtains an import path for a generated Protobuf file.
     *
     * @param importPath
     *         the name of a JavaScript file
     * @return the import path obtaining by composing the module and file name
     * @throws IllegalStateException
     *         if the file is not a generated Protobuf
     *         or is not provided by the module
     */
    ImportPath importPathFor(ImportPath importPath) {
        checkState(importPath.fileName()
                             .isGeneratedProto());
        checkState(provides(importPath));
        String path = name + ImportPath.separator() + importPath;
        return ImportPath.of(path);
    }

    /**
     * Checks if the module provides imported file.
     *
     * @param importPath
     *         the import to check
     * @return {@code true} if the module provides the file
     */
    boolean provides(ImportPath importPath) {
        PackageName packageName = importPath.fileName()
                                            .protoPackage();
        for (PackagePattern packagePattern : packages) {
            if (packagePattern.matches(packageName)) {
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
        if (!(o instanceof ExternalModule)) {
            return false;
        }
        ExternalModule module = (ExternalModule) o;
        return name.equals(module.name) &&
                packages.equals(module.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, packages);
    }
}
