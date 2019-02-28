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

import io.spine.tools.protoc.GeneratedMethod;
import io.spine.tools.protoc.GeneratedMethodsConfig;
import io.spine.tools.protoc.UuidMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratedMethods should")
final class GeneratedMethodsTest {

    @DisplayName("convert to proper Protoc configuration")
    @Test
    void convertToProperProtocConfiguration() {
        String testMethodFactory = "io.spine.test.MethodFactory";
        GeneratedMethods methods = GeneratedMethods.withDefaults();
        methods.useFactory(testMethodFactory, methods.uuidMessage());
        methods.useFactory(testMethodFactory, methods.filePattern()
                                                     .endsWith("_test.proto"));
        GeneratedMethodsConfig config = methods.asProtocConfig();

        assertEquals(testMethodFactory, config.getUuidMethod()
                                              .getFactoryName());
        assertEquals(testMethodFactory, config.getGeneratedMethod(0)
                                              .getFactoryName());
    }

    @DisplayName("add multiple file patterns")
    @Test
    void addMultipleFilePatterns() {
        String pattern = "testPattern";
        String interfaceName = "io.spine.test.TestInterface";

        GeneratedMethods defaults = GeneratedMethods.withDefaults();
        FilePatternFactory filePattern = defaults.filePattern();
        defaults.useFactory(interfaceName, filePattern.endsWith(pattern));
        defaults.useFactory(interfaceName, filePattern.startsWith(pattern));
        defaults.useFactory(interfaceName, filePattern.regex(pattern));

        assertTrue(hasPostfixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasPrefixConfig(pattern, interfaceName, defaults.asProtocConfig()));
        assertTrue(hasRegexConfig(pattern, interfaceName, defaults.asProtocConfig()));
    }

    @DisplayName("be able to ignore UUID message configuration")
    @Test
    void ignoreUuidMessageConfig() {
        String testMethodFactory = "io.spine.test.MethodFactory";
        GeneratedMethods methods = GeneratedMethods.withDefaults();
        methods.useFactory(testMethodFactory, methods.uuidMessage());
        methods.ignore(methods.uuidMessage());
        assertSame(UuidMethod.getDefaultInstance(), methods.asProtocConfig()
                                                           .getUuidMethod());
    }

    private static boolean
    hasPostfixConfig(String postfix, String interfaceName, GeneratedMethodsConfig config) {
        for (GeneratedMethod generatedInterface : config.getGeneratedMethodList()) {
            if (postfix.equals(generatedInterface.getPattern()
                                                 .getFilePostfix()) &&
                    interfaceName.equals(generatedInterface.getFactoryName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean
    hasPrefixConfig(String prefix, String interfaceName, GeneratedMethodsConfig config) {
        for (GeneratedMethod generatedInterface : config.getGeneratedMethodList()) {
            if (prefix.equals(generatedInterface.getPattern()
                                                .getFilePrefix()) &&
                    interfaceName.equals(generatedInterface.getFactoryName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean
    hasRegexConfig(String regex, String interfaceName, GeneratedMethodsConfig config) {
        for (GeneratedMethod generatedInterface : config.getGeneratedMethodList()) {
            if (regex.equals(generatedInterface.getPattern()
                                               .getRegex()) &&
                    interfaceName.equals(generatedInterface.getFactoryName())) {
                return true;
            }
        }
        return false;
    }
}
