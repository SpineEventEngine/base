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
package io.spine.tools.mc.java.codegen

import com.google.common.truth.Truth.assertThat
import io.spine.tools.mc.java.gradle.McJavaExtension
import io.spine.tools.mc.java.gradle.McJavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class `'codegen { }' block should` {

    private lateinit var extension: McJavaExtension

    @BeforeEach
    fun prepareExtension() {
        val project = ProjectBuilder.builder().build()
        project.apply {
            it.plugin("java")
            it.plugin(McJavaPlugin::class.java)
        }
        extension = project.extensions.getByType(McJavaExtension::class.java)
    }

    @Test
    @DisplayName("apply changes immediately")
    fun immediately() {
        val factoryName = "fake.Factory"
        extension.codegen { config ->
            config.forUuids {
                it.generateMethodsWith(factoryName)
            }
        }
        val config = extension.codegen.toProto()
        assertThat(config.uuids.methodFactoryList)
            .hasSize(1)
        assertThat(
            config.uuids
                .methodFactoryList[0]
                .className
                .canonical
        ).isEqualTo(factoryName)
    }

    @Nested
    inner class configure {

        @Test
        fun commands() {
            val firstInterface = "test.iface.Command"
            val secondInterface = "test.iface.TestCommand"
            val fieldSuperclass = "test.cmd.Field"
            val suffix = "_my_commands.proto"
            extension.codegen { config: Codegen ->
                config.forCommands { commands: SignalConfig ->
                    commands.includeFiles(commands.by().suffix(suffix))
                    commands.markAs(firstInterface)
                    commands.markAs(secondInterface)
                    commands.markFieldsAs(fieldSuperclass)
                }
            }
            val config = extension.codegen.toProto()
            val commands = config.commands
            assertThat(commands.patternList)
                .hasSize(1)
            assertThat(commands.patternList[0].suffix)
                .isEqualTo(suffix)
            assertThat(commands.addInterfaceList.map { it.name.canonical })
                .containsExactly(firstInterface, secondInterface)
            assertThat(commands.generateFields.superclass.canonical)
                .isEqualTo(fieldSuperclass)
        }

        @Test
        fun events() {
            val iface = "test.iface.Event"
            val fieldSuperclass = "test.cmd.Field"
            val prefix = "my_"
            extension.codegen { config: Codegen ->
                config.forEvents { events: SignalConfig ->
                    events.includeFiles(events.by().prefix(prefix))
                    events.markAs(iface)
                    events.markFieldsAs(fieldSuperclass)
                }
            }
            val config = extension.codegen.toProto()
            val events = config.events
            assertThat(events.patternList)
                .hasSize(1)
            assertThat(events.patternList[0].prefix)
                .isEqualTo(prefix)
            assertThat(events.addInterfaceList.map { it.name.canonical })
                .containsExactly(iface)
            assertThat(events.generateFields.superclass.canonical)
                .isEqualTo(fieldSuperclass)
        }
    }
}
