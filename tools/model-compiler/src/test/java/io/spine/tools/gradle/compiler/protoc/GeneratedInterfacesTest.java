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
import io.spine.tools.protoc.AddInterfaces;
import io.spine.tools.protoc.ConfigByPattern;
import io.spine.tools.protoc.FilePattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static io.spine.base.MessageFile.COMMANDS;
import static io.spine.base.MessageFile.EVENTS;
import static io.spine.base.MessageFile.REJECTIONS;
import static io.spine.tools.gradle.compiler.protoc.MessageSelectorFactory.prefix;
import static io.spine.tools.gradle.compiler.protoc.MessageSelectorFactory.regex;
import static io.spine.tools.gradle.compiler.protoc.MessageSelectorFactory.suffix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratedInterfaces should")
final class GeneratedInterfacesTest {

    @DisplayName("prepare default GeneratedInterfaceConfig for")
    @Nested
    class Default {

        @DisplayName("UuidValue")
        @Test
        void uuid() {
            AddInterfaces defaults = GeneratedInterfaces.withDefaults()
                                                        .asProtocConfig();
            assertHasInterface(UuidValue.class, defaults.getUuidInterface()
                                                        .getValue());
        }

        @DisplayName("CommandMessage")
        @Test
        void command() {
            AddInterfaces defaults = GeneratedInterfaces.withDefaults()
                                                        .asProtocConfig();
            assertHasInterfaceWithNameAndSuffix(CommandMessage.class, COMMANDS.suffix(), defaults);
        }

        @DisplayName("EventMessage")
        @Test
        void event() {
            AddInterfaces defaults = GeneratedInterfaces.withDefaults()
                                                        .asProtocConfig();
            assertHasInterfaceWithNameAndSuffix(EventMessage.class, EVENTS.suffix(), defaults);
        }

        @DisplayName("RejectionMessage")
        @Test
        void rejection() {
            AddInterfaces defaults = GeneratedInterfaces.withDefaults()
                                                        .asProtocConfig();
            assertHasInterfaceWithNameAndSuffix(RejectionMessage.class, REJECTIONS.suffix(),
                                                defaults);
        }

        private void assertHasInterfaceWithNameAndSuffix(Class<?> interfaceClass,
                                                         String suffix,
                                                         AddInterfaces config) {
            String expectedInterface = interfaceClass.getName();
            assertTrue(hasSuffixConfig(suffix, expectedInterface, config));
        }
    }

    @DisplayName("add multiple file patterns")
    @Test
    void addMultipleFilePatterns() {
        String pattern = "testPattern";
        String interfaceName = "io.spine.test.TestInterface";

        GeneratedInterfaces defaults = GeneratedInterfaces.withDefaults();
        MessageSelectorFactory messages = defaults.messages();
        defaults.mark(messages.inFiles(suffix(pattern)), interfaceName);
        defaults.mark(messages.inFiles(prefix(pattern)), interfaceName);
        defaults.mark(messages.inFiles(regex(pattern)), interfaceName);

        assertTrue(hasSuffixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasPrefixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasRegexConfig(pattern, interfaceName, defaults.asProtocConfig()));
    }

    private static boolean
    hasSuffixConfig(String suffix, String interfaceName, AddInterfaces config) {
        return hasInterface(config, interfaceName,
                            pattern -> suffix.equals(pattern.getSuffix()));
    }

    private static boolean
    hasPrefixConfig(String prefix, String interfaceName, AddInterfaces config) {
        return hasInterface(config, interfaceName,
                            pattern -> prefix.equals(pattern.getPrefix()));
    }

    private static boolean
    hasRegexConfig(String regex, String interfaceName, AddInterfaces config) {
        return hasInterface(config, interfaceName,
                            pattern -> regex.equals(pattern.getRegex()));
    }

    private static boolean hasInterface(AddInterfaces config,
                                        String interfaceName,
                                        Predicate<? super FilePattern> patternPredicate) {
        return config
                .getInterfaceByPatternList()
                .stream()
                .filter(byPattern -> interfaceName.equals(byPattern.getValue()))
                .map(ConfigByPattern::getPattern)
                .anyMatch(patternPredicate);
    }

    private static void assertHasInterface(Class<?> interfaceClass, String actualValue) {
        assertEquals(ClassName.of(interfaceClass), ClassName.of(actualValue));
    }
}
