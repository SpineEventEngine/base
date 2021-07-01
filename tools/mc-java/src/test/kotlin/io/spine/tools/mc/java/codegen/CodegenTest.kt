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
    inner class `configure generation of` {

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
            val fieldSuperclass = "test.event.Field"
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

        @Test
        fun rejections() {
            val iface = "test.iface.RejectionMessage"
            val fieldSuperclass = "test.rejection.Field"
            val regex = ".*rejection.*"
            extension.codegen { config: Codegen ->
                config.forEvents { events: SignalConfig ->
                    events.includeFiles(events.by().regex(regex))
                    events.markAs(iface)
                    events.markFieldsAs(fieldSuperclass)
                }
            }
            val config = extension.codegen.toProto()
            val events = config.events
            assertThat(events.patternList)
                .hasSize(1)
            assertThat(events.patternList[0].regex)
                .isEqualTo(regex)
            assertThat(events.addInterfaceList.map { it.name.canonical })
                .containsExactly(iface)
            assertThat(events.generateFields.superclass.canonical)
                .isEqualTo(fieldSuperclass)
        }

        @Test
        fun `rejections separately from events`() {
            val eventInterface = "test.iface.EventMsg"
            val rejectionInterface = "test.iface.RejectionMsg"
            extension.codegen { config ->
                config.forEvents {
                    it.markAs(eventInterface)
                }
                config.forRejections {
                    it.markAs(rejectionInterface)
                }
            }
            val config = extension.codegen.toProto()
            val eventInterfaces = config.events.addInterfaceList
            val rejectionInterfaces = config.rejections.addInterfaceList
            assertThat(eventInterfaces)
                .hasSize(1)
            assertThat(rejectionInterfaces)
                .hasSize(1)
            assertThat(eventInterfaces.first().name.canonical)
                .isEqualTo(eventInterface)
            assertThat(rejectionInterfaces.first().name.canonical)
                .isEqualTo(rejectionInterface)
        }

        @Test
        fun entities() {
            val iface = "custom.EntityMessage"
            val fieldSupertype = "custom.FieldSupertype"
            val suffix = "view.proto"
            val option = "view"
            extension.codegen { config ->
                config.forEntities {
                    it.options.add(option)
                    it.includeFiles(it.by().suffix(suffix))
                    it.skipQueries()
                    it.markAs(iface)
                    it.markFieldsAs(fieldSupertype)
                }
            }
            val config = extension.codegen.toProto().entities
            assertThat(config.addInterfaceList.map { it.name.canonical })
                .containsExactly(iface)
            assertThat(config.generateFields.superclass.canonical)
                .isEqualTo(fieldSupertype)
            assertThat(config.patternList)
                .hasSize(1)
            assertThat(config.patternList.first().suffix)
                .isEqualTo(suffix)
            assertThat(config.optionList)
                .hasSize(1)
            assertThat(config.optionList.first().name)
                .isEqualTo(option)
        }

        @Test
        fun `UUID messages`() {
            val iface = "custom.RandomizedId"
            val methodFactory = "custom.MethodFactory"
            extension.codegen { config ->
                config.forUuids {
                    it.markAs(iface)
                    it.generateMethodsWith(methodFactory)
                }
            }
            val config = extension.codegen.toProto().uuids
            assertThat(config.addInterfaceList.map { it.name.canonical })
                .containsExactly(iface)
            assertThat(config.methodFactoryList)
                .hasSize(1)
            assertThat(config.methodFactoryList.first().className.canonical)
                .isEqualTo(methodFactory)
        }

        @Test
        fun `an arbitrary message groups`() {
            val firstInterface = "com.acme.Foo"
            val secondInterface = "com.acme.Bar"
            val methodFactory = "custom.MethodFactory"
            val classFactory = "custom.NestedClassFactory"
            val fieldSuperclass = "acme.Searchable"
            val firstMessageType = "acme.small.yellow.Bird"
            extension.codegen { config ->
                config.forMessage(firstMessageType) {
                    it.markAs(firstInterface)
                    it.markFieldsAs(fieldSuperclass)
                    it.generateNestedClassesWith(classFactory)
                }
                config.forMessages(config.by().regex(".+_.+")) {
                    it.markAs(secondInterface)
                    it.generateMethodsWith(methodFactory)
                }
            }
            val configs = extension.codegen.toProto().messagesList
            assertThat(configs)
                .hasSize(2)
            val (first, second) = configs
            assertThat(first.pattern.type.expectedType.value)
                .isEqualTo(firstMessageType)
            assertThat(first.addInterfaceList.first().name.canonical)
                .isEqualTo(firstInterface)
            assertThat(first.generateFields.superclass.canonical)
                .isEqualTo(fieldSuperclass)
            assertThat(first.generateNestedClassesList)
                .hasSize(1)
            assertThat(first.generateNestedClassesList.first().factory.className.canonical)
                .isEqualTo(classFactory)

            assertThat(second.pattern.file.hasRegex())
                .isTrue()
            assertThat(second.addInterfaceList.first().name.canonical)
                .isEqualTo(secondInterface)
            assertThat(second.generateMethodsList.first().factory.className.canonical)
                .isEqualTo(methodFactory)
        }
    }
}
