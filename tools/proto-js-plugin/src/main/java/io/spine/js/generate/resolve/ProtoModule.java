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
import io.spine.code.js.FileName;
import io.spine.code.js.ImportPath;
import io.spine.code.proto.PackageName;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A description of a JavaScript module, which provides compiled Protobuf files.
 *
 * <p>Describes what Protobuf files are provided using the
 * {@linkplain PackagePattern patterns} of Protobuf packages.
 */
public final class ProtoModule {

    private final String name;
    private final List<PackagePattern> packages;

    /**
     * Creates a new instance.
     *
     * @param name
     *         the name of the module to be resolved, e.g. {@code library/proto}
     * @param packages
     *         patterns of packages provided by the module
     */
    public ProtoModule(String name, List<PackagePattern> packages) {
        this.name = checkNotEmptyOrBlank(name);
        this.packages = ImmutableList.copyOf(packages);
    }

    /**
     * Resolves a relative path to a generated Protobuf file.
     *
     * <p>As a result, an import path like {@code ../../spine/core/user_id_pb.js}
     * becomes {@code moduleName/spine/core/user_id_pb.js} path.
     */
    ImportPath resolve(ImportPath importPath) {
        FileName fileName = importPath.fileName();
        PackageName packageName = fileName.protoPackage();
        checkState(provides(packageName));
        String resolved = name + ImportPath.separator() + fileName;
        return ImportPath.of(resolved);
    }

    /**
     * Checks if the Protobuf package is provided by the module.
     *
     * @param packageName
     *         the package to check
     * @return {@code true} if the module provides the package
     */
    boolean provides(PackageName packageName) {
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
        if (!(o instanceof ProtoModule)) {
            return false;
        }
        ProtoModule module = (ProtoModule) o;
        return name.equals(module.name) &&
                packages.equals(module.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, packages);
    }
}
