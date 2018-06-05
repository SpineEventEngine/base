/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.json;

import com.google.protobuf.util.JsonFormat.TypeRegistry;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;

/**
 * A factory of {@link TypeRegistry} instances.
 *
 * @author Dmytro Dashenkov
 */
final class TypeRegistries {

    /**
     * Prevents the utility class instantiation.
     */
    private TypeRegistries() {
    }

    /**
     * Creates a type registry of all known types.
     *
     * <p>The registry is built from the <a href="https://github.com/google/protobuf-gradle-plugin/blob/master/README.md#generate-descriptor-set-files">
     * descriptor set files</a> found in the classpath.
     *
     * @return a {@link TypeRegistry} of all the known types
     */
    static TypeRegistry ofKnownTypes() {
        // TODO:2018-06-04:dmytro.dashenkov: Move to KnownTypes.
        FileSet files = FileSet.load();
        final TypeSet types = TypeSet.messagesAndEnums(files);
        final TypeRegistry typeRegistry = types.toJsonPrinterRegistry();
        return typeRegistry;
    }
}
