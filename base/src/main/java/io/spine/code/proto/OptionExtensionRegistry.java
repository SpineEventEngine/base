/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Extension;
import com.google.protobuf.ExtensionRegistry;
import io.spine.option.OptionsProto;
import io.spine.validate.option.ValidatingOptionFactory;
import io.spine.validate.option.ValidatingOptionsLoader;

/**
 * A registry that contains all of Protobuf option extensions.
 *
 * <p>Calling {@link OptionExtensionRegistry#instance()} obtains a registry with all of the options
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
     * Creates an {@link ExtensionRegistry} with all the {@code
     * spine/options.proto} extensions.
     */
    private static ExtensionRegistry optionExtensions() {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        OptionsProto.registerAllExtensions(registry);
        registerCustomOptions(registry);
        return registry;
    }

    private static void registerCustomOptions(ExtensionRegistry target) {
        ImmutableSet<ValidatingOptionFactory> implementations =
                ValidatingOptionsLoader.INSTANCE.implementations();
        implementations.stream()
                       .flatMap(factory -> factory.all().stream())
                       .map(AbstractOption::extension)
                       .filter(extension -> isExtensionRegistered(target, extension))
                       .forEach(target::add);
    }

    private static boolean isExtensionRegistered(ExtensionRegistry registry,
                                                 Extension<?, ?> extension) {
        String name = extension.getDescriptor()
                               .getFullName();
        boolean mutableAbsent = registry.findMutableExtensionByName(name) == null;
        boolean immutableAbsent = registry.findImmutableExtensionByName(name) == null;
        return mutableAbsent && immutableAbsent;
    }
}
