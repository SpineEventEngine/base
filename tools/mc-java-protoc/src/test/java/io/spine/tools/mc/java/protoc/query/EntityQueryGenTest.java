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

package io.spine.tools.mc.java.protoc.query;

import com.google.common.testing.NullPointerTester;
import io.spine.option.OptionsProto;
import io.spine.tools.mc.java.protoc.CodeGenerator;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.mc.java.protoc.NoOpGenerator;
import io.spine.tools.protoc.Entities;
import io.spine.tools.protoc.ProtoOption;
import io.spine.tools.protoc.SpineProtocConfig;
import io.spine.tools.protoc.plugin.nested.Task;
import io.spine.tools.protoc.plugin.nested.TaskView;
import io.spine.type.EnumType;
import io.spine.type.MessageType;
import io.spine.type.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.mc.java.protoc.Generators.generate;

@DisplayName("`EntityQueryGen` should")
class EntityQueryGenTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicStaticMethods(EntityQueryGen.class);
    }

    @Test
    @DisplayName("generate code for entities if requested types where appropriate")
    void generateWhenRequired() {
        SpineProtocConfig config = newConfig();

        CodeGenerator generator = EntityQueryGen.instance(config);
        assertThat(generator)
                .isInstanceOf(EntityQueryGen.class);
        Type<?, ?> type = new MessageType(TaskView.getDescriptor());
        Collection<CompilerOutput> output = generate(generator, type);
        assertThat(output)
                .isNotEmpty();
    }

    @Test
    @DisplayName("ignore non-message types")
    void enums() {
        SpineProtocConfig config = newConfig();

        CodeGenerator generator = EntityQueryGen.instance(config);
        assertThat(generator)
                .isInstanceOf(EntityQueryGen.class);
        Type<?, ?> type = EnumType.create(Task.Priority.getDescriptor());
        Collection<CompilerOutput> output = generate(generator, type);

        assertThat(output)
                .isEmpty();
    }

    @Test
    @DisplayName("do nothing if turned off")
    void off() {
        SpineProtocConfig config = newConfig(false);

        CodeGenerator generator = EntityQueryGen.instance(config);
        assertThat(generator)
                .isInstanceOf(NoOpGenerator.class);
    }

    private static SpineProtocConfig newConfig() {
        return newConfig(true);
    }

    private static SpineProtocConfig newConfig(boolean generate) {
        Entities.Builder entities = Entities.newBuilder();
        entities.addOption(ProtoOption.newBuilder()
                .setName(OptionsProto.entity.getDescriptor().getName())
        );
        entities.setGenerateQueries(generate);
        return SpineProtocConfig.newBuilder()
                .setEntities(entities)
                .build();
    }
}
