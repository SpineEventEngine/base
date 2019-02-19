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

package io.spine.option;

import com.google.protobuf.ExtensionRegistry;
import io.spine.annotation.Internal;

/**
 * A registry that contains all of Protobuf option extensions.
 *
 * <p>Calling {@link OptionExtensionRegistry#instance()} obtains a registry with all of the options
 * defined in Spine.
 *
 * @apiNote Use this instead of accessing {@link ExtensionRegistry#newInstance()} directly, since
 * {@code ExtensionRegistry} doesn't have any of the Spine options by default.
 */
@Internal
public final class OptionExtensionRegistry {
    private static final com.google.protobuf.ExtensionRegistry EXTENSIONS = optionExtensions();

    /**
     * Prevents the utility class instantiation.
     */
    private OptionExtensionRegistry() {
    }

    /**
     * Obtains the {@link com.google.protobuf.ExtensionRegistry} with all the {@code
     * spine/options.proto} extensions.
     */
    public static com.google.protobuf.ExtensionRegistry instance() {
        return EXTENSIONS;
    }

    /**
     * Creates an {@link com.google.protobuf.ExtensionRegistry} with all the {@code
     * spine/options.proto} extensions.
     */
    private static com.google.protobuf.ExtensionRegistry optionExtensions() {
        com.google.protobuf.ExtensionRegistry registry = com.google.protobuf.ExtensionRegistry.newInstance();
        OptionsProto.registerAllExtensions(registry);
        return registry;
    }
}
