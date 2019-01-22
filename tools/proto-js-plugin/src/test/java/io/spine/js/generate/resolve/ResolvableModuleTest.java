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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ResolvableModule should")
class ResolvableModuleTest {

    private static final String moduleName = "spine-js";
    private static final List<PackagePattern> patterns = ImmutableList.of(
            PackagePattern.of("spine")
    );
    private final ResolvableModule module = new ResolvableModule(moduleName, patterns);

    @Test
    @DisplayName("not have an empty module name")
    void notHaveEmptyModuleName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResolvableModule("", patterns)
        );
    }

    @Test
    @DisplayName("resolve an import path if the file is provided by module")
    void resolveMatchingImport() {
        ImportPath resolvable = ImportPath.of("../../spine/js_pb.js");
        ImportPath resolved = module.resolve(resolvable);
        ImportPath expected = ImportPath.of(moduleName + "/spine/js_pb.js");
        assertEquals(expected, resolved);
    }

    @Test
    @DisplayName("resolve only Proto files")
    void acceptOnlyProto() {
        ImportPath importPath = ImportPath.of("non-proto.js");
        assertThrows(
                IllegalStateException.class,
                () -> module.resolve(importPath)
        );
    }
    
    @Test
    @DisplayName("resolve only provided Proto files")
    void acceptOnlyProvidedProto() {
        ImportPath importPath = ImportPath.of("non/spine/index_pb.js");
        assertThrows(
                IllegalStateException.class,
                () -> module.resolve(importPath)
        );
    }
}
