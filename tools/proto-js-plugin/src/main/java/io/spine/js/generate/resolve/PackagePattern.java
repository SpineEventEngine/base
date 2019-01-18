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

import io.spine.code.js.ImportPath;

import java.util.regex.Pattern;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A pattern of a Protobuf package.
 *
 * <p>Used to resolve relative imports of Protobuf files.
 */
class PackagePattern {

    private static final String PACKAGE_SEPARATOR = "\\.";
    private static final Pattern PACKAGE_SEPARATOR_PATTERN = Pattern.compile(PACKAGE_SEPARATOR);

    private final String packageName;

    private PackagePattern(String packageName) {
        this.packageName = checkNotEmptyOrBlank(packageName);
    }

    /**
     * Creates a new instance.
     *
     * @param packageName
     *         the name of a Protobuf package
     * @return a new instance
     */
    static PackagePattern of(String packageName) {
        return new PackagePattern(packageName);
    }

    /**
     * Checks if the imported file matches the pattern.
     */
    boolean matches(ImportPath importPath) {
        String strippedPath = importPath.stripRelativePath();
        String packageAsPath = packagePath();
        boolean rootPackageMatches = strippedPath.startsWith(packageAsPath);
        if (!rootPackageMatches) {
            return false;
        }
        String pathWithoutRootPackage = strippedPath.substring(packageAsPath.length() + 1);
        boolean exactlyInRootPackage = !pathWithoutRootPackage.contains(ImportPath.separator());
        return exactlyInRootPackage;
    }

    private String packagePath() {
        return PACKAGE_SEPARATOR_PATTERN.matcher(packageName)
                                        .replaceAll(ImportPath.separator());
    }
}
