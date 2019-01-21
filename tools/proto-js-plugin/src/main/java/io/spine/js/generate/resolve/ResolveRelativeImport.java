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
import io.spine.logging.Logging;

import java.nio.file.Path;
import java.util.List;

/**
 * Resolves a relative import path among the {@link #modules}.
 */
final class ResolveRelativeImport extends ResolveAction implements Logging {

    private final Directory generatedRoot;
    private final List<ResolvableModule> modules;

    ResolveRelativeImport(Directory generatedRoot, List<ResolvableModule> modules) {
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
        for (ResolvableModule module : modules) {
            if (module.matches(importPath)) {
                return module.resolve(resolvable);
            }
        }
        return resolvable;
    }

    @Override
    boolean skipForModule(ImportPath importPath) {
        String pathFromRoot = importPath.stripRelativePath();
        //TODO:2019-01-18:dmytro.grankin: consider check presence of .proto file instead.
        Path absolutePath = generatedRoot.getPath()
                                         .resolve(pathFromRoot);
        boolean presentInModule = absolutePath.toFile()
                                              .exists();
        _debug("Checking if the file {} belongs to the module Protobuf sources, result: {}",
               absolutePath, presentInModule);
        return presentInModule;
    }
}
