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

package io.spine.tools.gradle.compiler.protoc;

import io.spine.base.CommandMessage;
import io.spine.base.EnrichmentMessage;
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.GeneratedInterface;
import io.spine.tools.protoc.GeneratedInterfacesConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.spine.base.MessageFile.COMMANDS;
import static io.spine.base.MessageFile.EVENTS;
import static io.spine.base.MessageFile.REJECTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratedInterfaces should prepare default GeneratedInterfaceConfig for")
class GeneratedInterfacesTest {

    @DisplayName("EnrichmentMessage")
    @Test
    void enrichment() {
        GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
        assertHasInterfaceName(EnrichmentMessage.class, defaults.enrichmentMessage());
    }

    @DisplayName("UuidValue")
    @Test
    void uuid() {
        GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
        assertHasInterfaceName(UuidValue.class, defaults.uuidMessage());
    }

    @DisplayName("CommandMessage")
    @Test
    void command() {
        GeneratedInterfacesConfig defaults = GeneratedInterfaces.withDefaults()
                                                                .asProtocConfig();
        assertHasInterfaceWithNameAndPostfix(CommandMessage.class, COMMANDS.suffix(), defaults);
    }

    @DisplayName("EventMessage")
    @Test
    void event() {
        GeneratedInterfacesConfig defaults = GeneratedInterfaces.withDefaults()
                                                                .asProtocConfig();
        assertHasInterfaceWithNameAndPostfix(EventMessage.class, EVENTS.suffix(), defaults);
    }

    @DisplayName("RejectionMessage")
    @Test
    void rejection() {
        GeneratedInterfacesConfig defaults = GeneratedInterfaces.withDefaults()
                                                                .asProtocConfig();
        assertHasInterfaceWithNameAndPostfix(RejectionMessage.class, REJECTIONS.suffix(), defaults);
    }

    void assertHasInterfaceWithNameAndPostfix(Class<?> interfaceClass,
                                              String postfix,
                                              GeneratedInterfacesConfig config) {
        boolean hasInterface = false;
        String expectedInterface = interfaceClass.getName();
        for (GeneratedInterface generatedInterface : config.getGeneratedInterfaceList()) {
            if (expectedInterface.equals(generatedInterface.getInterfaceName()) &&
                    postfix.equals(generatedInterface.getFilter()
                                                     .getFilePostfix())) {
                hasInterface = true;
                break;
            }
        }
        assertTrue(hasInterface);
    }

    void assertHasInterfaceName(Class<?> interfaceClass, GeneratedInterfaceConfig config) {
        Optional<ClassName> actual = Optional.ofNullable(config.interfaceName());
        assertTrue(actual.isPresent());
        assertEquals(ClassName.of(interfaceClass), actual.get());
    }
}
