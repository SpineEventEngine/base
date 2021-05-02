/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.plugin.given;

import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.tools.java.code.UuidMethodFactory;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.Interfaces;
import io.spine.tools.protoc.MessageSelectorFactory;
import io.spine.tools.protoc.Methods;
import io.spine.tools.protoc.SpineProtocConfig;

import static io.spine.base.MessageFile.COMMANDS;
import static io.spine.base.MessageFile.EVENTS;
import static io.spine.base.MessageFile.REJECTIONS;
import static io.spine.tools.protoc.MessageSelectorFactory.suffix;

/**
 * A helper class for {@link io.spine.tools.protoc.SpineProtocConfig}s creation.
 */
public final class SpineProtocConfigGiven {

    /** Prevents instantiation of this utility class. */
    private SpineProtocConfigGiven() {
    }

    /**
     * Creates an instance of the {@link SpineProtocConfig} that represents the default Spine
     * model compiler configuration.
     */
    public static SpineProtocConfig defaultProtocConfig() {
        return SpineProtocConfig
                .newBuilder()
                .setAddInterfaces(defaultInterfaces().asProtocConfig())
                .setAddMethods(defaultMethods().asProtocConfig())
                .build();
    }

    /**
     * Creates an instance of the {@link Interfaces} that represents the default Spine
     * model compiler configuration.
     */
    public static Interfaces defaultInterfaces() {
        Interfaces config = new Interfaces();
        MessageSelectorFactory messages = config.messages();
        config.mark(messages.inFiles(suffix(COMMANDS.suffix())),
                    ClassName.of(CommandMessage.class));
        config.mark(messages.inFiles(suffix(EVENTS.suffix())),
                    ClassName.of(EventMessage.class));
        config.mark(messages.inFiles(suffix(REJECTIONS.suffix())),
                    ClassName.of(RejectionMessage.class));
        config.mark(messages.uuid(), ClassName.of(UuidValue.class));
        config.mark(messages.entityState(), ClassName.of(EntityState.class));
        return config;
    }

    /**
     * Creates an instance of the {@link Methods} that represents the default Spine
     * model compiler configuration.
     */
    public static Methods defaultMethods() {
        Methods methods = new Methods();
        MessageSelectorFactory messages = methods.messages();
        methods.applyFactory(UuidMethodFactory.class.getName(), messages.uuid());
        return methods;
    }
}