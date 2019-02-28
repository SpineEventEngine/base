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
import io.spine.base.EventMessage;
import io.spine.base.RejectionMessage;
import io.spine.base.UuidValue;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.GeneratedInterface;
import io.spine.tools.protoc.GeneratedInterfacesConfig;
import io.spine.tools.protoc.UuidInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.base.MessageFile.COMMANDS;
import static io.spine.base.MessageFile.EVENTS;
import static io.spine.base.MessageFile.REJECTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratedInterfaces should")
final class GeneratedInterfacesTest {

    @DisplayName("prepare default GeneratedInterfaceConfig for")
    @Nested
    class Default {

        @DisplayName("UuidValue")
        @Test
        void uuid() {
            GeneratedInterfacesConfig defaults = GeneratedInterfaces.withDefaults()
                                                                    .asProtocConfig();
            assertHasInterface(UuidValue.class, defaults.getUuidInterface()
                                                        .getInterfaceName());
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
            assertHasInterfaceWithNameAndPostfix(RejectionMessage.class, REJECTIONS.suffix(),
                                                 defaults);
        }

        private void assertHasInterfaceWithNameAndPostfix(Class<?> interfaceClass,
                                                          String postfix,
                                                          GeneratedInterfacesConfig config) {
            String expectedInterface = interfaceClass.getName();
            assertTrue(hasPostfixConfig(postfix, expectedInterface, config));
        }
    }

    @DisplayName("be able to ignore default GeneratedInterfaceConfig for")
    @Nested
    class IgnoreDefault {

        @DisplayName("UuidValue")
        @Test
        void uuid() {
            GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
            defaults.ignore(defaults.uuidMessage());
            GeneratedInterfacesConfig protocConfig = defaults.asProtocConfig();
            assertSame(UuidInterface.getDefaultInstance(), protocConfig.getUuidInterface());
        }

        @DisplayName("CommandMessage")
        @Test
        void command() {
            GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
            defaults.ignore(defaults.filePattern()
                                    .endsWith(COMMANDS.suffix()));
            assertHasNot(COMMANDS.suffix(), defaults.asProtocConfig());
        }

        @DisplayName("EventMessage")
        @Test
        void event() {
            GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
            defaults.ignore(defaults.filePattern()
                                    .endsWith(EVENTS.suffix()));
            assertHasNot(EVENTS.suffix(), defaults.asProtocConfig());
        }

        @DisplayName("RejectionMessage")
        @Test
        void rejection() {
            GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
            defaults.ignore(defaults.filePattern()
                                    .endsWith(REJECTIONS.suffix()));
            assertHasNot(REJECTIONS.suffix(), defaults.asProtocConfig());
        }

        private void assertHasNot(String prefix, GeneratedInterfacesConfig config) {
            boolean hasPattern = false;
            for (GeneratedInterface generatedInterface : config.getGeneratedInterfaceList()) {
                if (generatedInterface.getPattern()
                                      .getFilePrefix()
                                      .equalsIgnoreCase(prefix)) {
                    hasPattern = true;
                }
            }
            assertFalse(hasPattern);
        }
    }

    @DisplayName("add multiple file patterns")
    @Test
    void addMultipleFilePatterns() {
        String pattern = "testPattern";
        String interfaceName = "io.spine.test.TestInterface";

        GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
        FilePatternFactory filePattern = defaults.filePattern();
        defaults.mark(filePattern.endsWith(pattern), interfaceName);
        defaults.mark(filePattern.startsWith(pattern), interfaceName);
        defaults.mark(filePattern.regex(pattern), interfaceName);

        assertTrue(hasPostfixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasPrefixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasRegexConfig(pattern, interfaceName, defaults.asProtocConfig()));
    }

    private static boolean
    hasPostfixConfig(String postfix, String interfaceName, GeneratedInterfacesConfig config) {
        for (GeneratedInterface generatedInterface : config.getGeneratedInterfaceList()) {
            if (postfix.equals(generatedInterface.getPattern()
                                                 .getFilePostfix()) &&
                    interfaceName.equals(generatedInterface.getInterfaceName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean
    hasPrefixConfig(String prefix, String interfaceName, GeneratedInterfacesConfig config) {
        for (GeneratedInterface generatedInterface : config.getGeneratedInterfaceList()) {
            if (prefix.equals(generatedInterface.getPattern()
                                                .getFilePrefix()) &&
                    interfaceName.equals(generatedInterface.getInterfaceName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean
    hasRegexConfig(String regex, String interfaceName, GeneratedInterfacesConfig config) {
        for (GeneratedInterface generatedInterface : config.getGeneratedInterfaceList()) {
            if (regex.equals(generatedInterface.getPattern()
                                               .getRegex()) &&
                    interfaceName.equals(generatedInterface.getInterfaceName())) {
                return true;
            }
        }
        return false;
    }

    private static void assertHasInterface(Class<?> interfaceClass, String actualValue) {
        assertEquals(ClassName.of(interfaceClass), ClassName.of(actualValue));
    }
}
