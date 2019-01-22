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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.spine.code.js.Directory;
import io.spine.code.js.FileName;
import io.spine.code.js.ImportPath;
import io.spine.logging.Logging;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.js.ImportPath.parentDirectory;

/**
 * An action replacing import paths of Spine library files by relative import paths.
 *
 * <p>The class is originally created to replace imports of the
 * {@linkplain io.spine.code.js.Module#spineWeb Spine Web} in Protobuf files
 * provided by the Spine Web itself.
 */
final class ResolveSpineImport extends ResolveAction implements Logging {

    private static final String SRC_RELATIVE_TO_MAIN_PROTO = parentDirectory();
    /**
     * The relative path from the Protobuf root directory to the sources directory.
     *
     * <p>The path is specific to Spine Web.
     */
    private static final String MODULE_RELATIVE_TO_PROTO = Strings.repeat(parentDirectory(), 2);
    /** A part of the import path to the main sources directory. */
    private static final String PROJECT_SRC_DIR = "main";

    private final Directory generatedRoot;

    ResolveSpineImport(Directory generatedRoot) {
        super();
        checkNotNull(generatedRoot);
        this.generatedRoot = generatedRoot;
    }

    @Override
    ImportSnippet resolve(ImportSnippet resolvable) {
        ImportPath filePath = resolvable.path()
                                        .skipLibrary();
        String pathPrefix = fileRelativeToSources(generatedRoot, resolvable.fileName());
        ImportSnippet resolved = resolvable.replacePath(pathPrefix + filePath);
        return resolved;
    }

    @Override
    boolean isApplicableTo(ImportPath importPath) {
        return importPath.isSpine();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Skips the resolving of the import if the imported file doesn't belong
     * to sources of the currently processed module.
     *
     * <p>The method assumes the project structure similar to Spine Web.
     */
    @Override
    boolean shouldNotResolve(ImportPath importPath) {
        ImportPath filePath = importPath.skipLibrary();
        Path absolutePath = sourcesPath(generatedRoot).resolve(filePath.value());
        boolean presentInModule = absolutePath.toFile()
                                              .exists();
        _debug("Checking if the file {} belongs to the module sources, result: {}",
               absolutePath, presentInModule);
        return !presentInModule;
    }

    @VisibleForTesting
    static Path sourcesPath(Directory generatedRoot) {
        Path modulePath = generatedRoot.getPath()
                                       .resolve(MODULE_RELATIVE_TO_PROTO);
        Path result = modulePath.resolve(PROJECT_SRC_DIR)
                                .normalize();
        return result;
    }

    /**
     * Obtains the relative path to the sources directory from the path of the file.
     *
     * <p>The path should be relative and contain slashes as separators since it
     * will be used in JS imports.
     */
    @VisibleForTesting
    static String fileRelativeToSources(Directory generatedRoot, FileName fileName) {
        Path protoRootPath = generatedRoot.getPath();
        boolean isMainSourceSet = protoRootPath.toString()
                                               .contains(PROJECT_SRC_DIR);
        if (isMainSourceSet) {
            return SRC_RELATIVE_TO_MAIN_PROTO + fileName.pathToRoot();
        }
        String pathToParentDir = MODULE_RELATIVE_TO_PROTO + fileName.pathToRoot();
        return pathToParentDir + PROJECT_SRC_DIR + ImportPath.separator();
    }
}
