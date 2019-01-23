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

import io.spine.code.js.FileName;
import io.spine.code.js.ImportPath;
import io.spine.code.js.Module;
import io.spine.logging.Logging;

/**
 * An action replacing import paths of Spine library files by relative import paths.
 *
 * <p>The class is originally created to replace imports of the
 * {@linkplain io.spine.code.js.Module#spineWeb Spine Web} in Protobuf files
 * provided by the Spine Web itself.
 */
final class ResolveSpineImport extends ResolveAction implements Logging {

    @Override
    ImportSnippet resolve(ImportSnippet resolvable) {
        ImportPath importPath = resolvable.path();
        FileName fileReference = importPath.fileName();
        String unifiedReference = fileReference.value()
                                               .replace("main/", "");
        String moduleReference =
                Module.spineWeb.artifactName() + ImportPath.separator() + unifiedReference;
        ImportSnippet resolved = resolvable.replacePath(moduleReference);
        return resolved;
    }

    @Override
    boolean isApplicableTo(ImportPath importPath) {
        return importPath.isRelative();
    }
}
