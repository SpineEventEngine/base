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
import org.slf4j.Logger;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.js.ImportPath.parentDirectory;

/**
 * Replaces library-like imports by relative paths if the imported file
 * {@linkplain #belongsToModuleSources(String, Directory) belongs}
 * to the currently processed module.
 */
final class ResolveSpineImport {

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
        checkNotNull(generatedRoot);
        this.generatedRoot = generatedRoot;
    }

    ImportSnippet performFor(ImportSnippet resolvable) {
        if (isApplicableTo(resolvable)) {
            return attemptResolve(resolvable);
        }
        return resolvable;
    }

    boolean isApplicableTo(ImportSnippet resolvable) {
        return !resolvable.isSpine();
    }

    private ImportSnippet attemptResolve(ImportSnippet resolvable) {
        String filePath = resolvable.path()
                                    .filePath();
        if (!belongsToModuleSources(filePath, generatedRoot)) {
            return resolvable;
        }
        String pathPrefix = fileRelativeToSources(generatedRoot, resolvable.fileName());
        ImportSnippet resolved = resolvable.replacePath(pathPrefix + filePath);
        return resolved;
    }

    /**
     * Tells whether a file with the specified path belongs
     * to sources of the currently processed module.
     *
     * <p>The method assumes the project structure similar to Spine Web.
     */
    @VisibleForTesting
    static boolean belongsToModuleSources(String filePath, Directory generatedRoot) {
        Path absolutePath = sourcesPath(generatedRoot).resolve(filePath);
        boolean presentInModule = absolutePath.toFile()
                                              .exists();
        log().debug("Checking if the file {} belongs to the module sources, result: {}",
                    absolutePath, presentInModule);
        return presentInModule;
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

    private static Logger log() {
        return Logging.get(ResolveImports.class);
    }
}
