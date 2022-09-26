/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.code.proto;

import com.google.protobuf.ExtensionRegistry;
import io.spine.option.OptionsProvider;

import java.util.ServiceLoader;

/**
 * A registry that contains all of Protobuf option extensions.
 *
 * <p>Calling {@link OptionExtensionRegistry#instance()} obtains a registry with all the options
 * defined in Spine.
 *
 * @apiNote Use this instead of accessing {@link ExtensionRegistry#newInstance()} directly, since
 * {@code ExtensionRegistry} doesn't have any of the Spine options by default.
 */
public final class OptionExtensionRegistry {
    private static final ExtensionRegistry EXTENSIONS = optionExtensions();

    /**
     * Prevents the utility class instantiation.
     */
    private OptionExtensionRegistry() {
    }

    /**
     * Obtains the {@link ExtensionRegistry} with all the {@code spine/options.proto} extensions.
     */
    public static ExtensionRegistry instance() {
        return EXTENSIONS;
    }

    /**
     * Creates an {@link ExtensionRegistry} performing the registration for all
     * {@link OptionsProvider}s discovered by the service loading mechanism in
     * the current classpath.
     */
    private static ExtensionRegistry optionExtensions() {
        var registry = ExtensionRegistry.newInstance();
        var loader = ServiceLoader.load(OptionsProvider.class);
        for (var provider : loader) {
            provider.registerIn(registry);
        }
        return registry;
    }
}
