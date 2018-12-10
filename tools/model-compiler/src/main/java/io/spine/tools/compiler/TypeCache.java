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
package io.spine.tools.compiler;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import io.spine.logging.Logging;
import io.spine.tools.gradle.compiler.RejectionGenPlugin;

import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A cache for the Protobuf message types parsed into appropriate Java types during
 * the Spine Rejection generation.
 *
 * @see RejectionGenPlugin
 */
public final class TypeCache implements Logging {

    /** A map from a fully-qualified Protobuf type name to fully-qualified Java class name. */
    private final Map<String, String> types = newHashMap();

    /**
     * Caches all the types declared in the file.
     *
     * @param file the descriptor of the file declaring types to cache
     */
    public void loadFrom(FileDescriptorProto file) {
        TypeLoader.load(this, file);
    }

    void put(String key, String value) {
        types.put(key, value);
    }

    /**
     * Obtain an immutable copy of the parsed Protobuf type names mapped to
     * FQN of related Java classes.
     *
     * @return current cache contents
     */
    public ImmutableMap<String, String> map() {
        ImmutableMap<String, String> immutable = ImmutableMap.copyOf(types);
        return immutable;
    }

    /**
     * Obtains all Java types collected by the type of the call.
     */
    public ImmutableCollection<String> javaTypes() {
        return map().values();
    }

    /**
     * Obtains Java type name for the passed proto type name.
     */
    public Optional<String> javaType(String protoType) {
        return Optional.ofNullable(types.get(protoType));
    }
}
