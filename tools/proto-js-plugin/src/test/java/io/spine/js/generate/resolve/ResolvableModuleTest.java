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
import com.google.protobuf.Any;
import io.spine.code.js.FileName;
import io.spine.code.js.ImportPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.resolve.given.Given.importWithPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ResolvableModule should")
class ResolvableModuleTest {

    private static final String moduleName = "spine-js";
    private final ResolvableModule module = new ResolvableModule(moduleName, ImmutableList.of(
            PackagePattern.of("spine")
    ));

    @Test
    @DisplayName("match an import path")
    void checkImportPathMatches() {
        assertMatches(module, "../spine/foo_pb.js");
        assertMatches(module, "../spine/bar_pb.js");
    }

    @Test
    @DisplayName("not match nested packages")
    void notMatchesNestedPackages() {
        assertNotMatches(module, "../spine/nested/type_pb.js");
    }

    @Test
    @DisplayName("resolve an import path if matches")
    void resolveMatchingImport() {
        FileName importSource = FileName.from(Any.getDescriptor()
                                                 .getFile());
        ImportSnippet resolvable = importWithPath("../spine/js_pb.js", importSource);
        ImportSnippet resolved = module.resolve(resolvable);
        assertEquals(resolved.path(), ImportPath.of(moduleName + "/spine/js_pb.js"));
    }

    private static void assertMatches(ResolvableModule module, String importPath) {
        ImportPath wrappedPath = ImportPath.of(importPath);
        boolean result = module.matches(wrappedPath);
        assertThat(result).isTrue();
    }

    private static void assertNotMatches(ResolvableModule module, String importPath) {
        ImportPath wrappedPath = ImportPath.of(importPath);
        boolean result = module.matches(wrappedPath);
        assertThat(result).isFalse();
    }
}
