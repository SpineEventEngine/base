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
import io.spine.logging.Logging;

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

    private final List<ProtoModule> modules;

    ResolveRelativeImport(List<ProtoModule> modules) {
        super();
        this.modules = ImmutableList.copyOf(modules);
    }

    @Override
    boolean isApplicableTo(ImportPath importPath) {
        boolean result = importPath.isRelative() && importPath.fileName()
                                                              .isGeneratedProto();
        return result;
    }

    @Override
    ImportSnippet resolve(ImportSnippet resolvable) {
        ImportPath importPath = resolvable.path();
        FileName fileName = importPath.fileName();
        PackageName packageName = fileName.protoPackage();
        for (ProtoModule module : modules) {
            if (module.provides(packageName)) {
                ImportPath pathInModule = module.importPathFor(fileName);
                return resolvable.replacePath(pathInModule.value());
            }
        }
        return resolvable;
    }
}
