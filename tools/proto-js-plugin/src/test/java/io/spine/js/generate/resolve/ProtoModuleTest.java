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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ProtoModule should")
class ProtoModuleTest {

    private static final String moduleName = "spine-js";
    private static final List<PackagePattern> patterns = ImmutableList.of(
            PackagePattern.of("spine")
    );
    private final ProtoModule module = new ProtoModule(moduleName, patterns);

    @Test
    @DisplayName("not have an empty module name")
    void notHaveEmptyModuleName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProtoModule("", patterns)
        );
    }

    @Test
    @DisplayName("resolve an import path if the file is provided by module")
    void resolveMatchingImport() {
        FileName fileName = FileName.of("spine/js_pb.js");
        ImportPath importPath = module.importPathFor(fileName);
        ImportPath expectedPath = ImportPath.of(moduleName + '/' + fileName);
        assertEquals(expectedPath, importPath);
    }

    @Test
    @DisplayName("compose an import path only for Proto files")
    void acceptOnlyProto() {
        FileName fileName = FileName.of("non-proto.js");
        assertThrows(
                IllegalStateException.class,
                () -> module.importPathFor(fileName)
        );
    }

    @Test
    @DisplayName("compose an import path only if package is provided by the module")
    void acceptOnlyProvidedProto() {
        FileName fileName = FileName.of("non/spine/index_pb.js");
        assertThrows(
                IllegalStateException.class,
                () -> module.importPathFor(fileName)
        );
    }
}
