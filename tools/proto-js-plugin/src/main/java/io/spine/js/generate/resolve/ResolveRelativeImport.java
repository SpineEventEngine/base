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
import io.spine.code.js.Directory;
import io.spine.code.js.ImportPath;
import io.spine.code.proto.PackageName;
import io.spine.logging.Logging;

import java.nio.file.Path;
import java.util.List;

/**
 * An action resolving relative imports of generated Protobuf files.
 *
 * <p>Currently, Protobuf compiler generates relative imports for dependencies of a file,
 * e.g. {@code require('../spine/options_pb.js')}, so the imported file
 * should be also generated even if it is provided by a library.
 *
 * <p>The action allows to replace relative imports by module imports,
 * so the import above can be resolved into {@code require('lib/spine/options_pb.js')}.
 *
 * <p>The knowledge about resolvable modules and Protobufs
 * they are provide is specified by the class users.
 */
final class ResolveRelativeImport extends ResolveAction implements Logging {

    private final Directory generatedRoot;
    private final List<ResolvableModule> modules;

    ResolveRelativeImport(Directory generatedRoot, List<ResolvableModule> modules) {
        super();
        this.generatedRoot = generatedRoot;
        this.modules = ImmutableList.copyOf(modules);
    }

    @Override
    boolean isApplicableTo(ImportPath importPath) {
        boolean result = importPath.isRelative() && importPath.isGeneratedProto();
        return result;
    }

    @Override
    ImportSnippet resolve(ImportSnippet resolvable) {
        ImportPath importPath = resolvable.path();
        PackageName packageName = importPath.protoPackage();
        for (ResolvableModule module : modules) {
            if (module.provides(packageName)) {
                ImportPath resolvedPath = module.resolve(importPath);
                return resolvable.replacePath(resolvedPath.value());
            }
        }
        return resolvable;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Skips if the imported file belongs to the module. A file is considered
     * belonging to the module if it is present in the folder with Protobufs compiled to JavaScript.
     */
    @Override
    boolean shouldNotResolve(ImportPath importPath) {
        String pathFromRoot = importPath.skipRelativePath();
        Path absolutePath = generatedRoot.getPath()
                                         .resolve(pathFromRoot);
        boolean presentInModule = absolutePath.toFile()
                                              .exists();
        _debug("Checking if the file {} belongs to the module Protobuf sources, result: {}",
               absolutePath, presentInModule);
        return presentInModule;
    }
}
