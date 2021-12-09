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

package io.spine.reflect;

import com.google.common.testing.EqualsTester;
import given.reflect.annotation.ValueAnnotation;
import given.reflect.root.branch1.bar.LastVisitor;
import given.reflect.root.branch1.foo.sub1.Sub1Class;
import given.reflect.root.branch1.foo.sub2.Sub2Class;
import given.reflect.root.branch2.lorem.ipsum.Sub3Class;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`PackageInfo` should")
class PackageInfoTest {

    private final PackageInfo javaUtil = PackageInfo.of(Collection.class.getPackage());
    private final PackageInfo javaUtilConcurrent = PackageInfo.of(Callable.class.getPackage());

    @Test
    @DisplayName("return package name in `toString()`")
    void stringify() {
        assertEquals(javaUtil.getValue()
                             .getName(),
                     javaUtil.toString());
    }

    @Test
    @DisplayName("have `equals()` and `hashCode()`")
    void hashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(javaUtil, PackageInfo.of(Collection.class.getPackage()))
                .addEqualityGroup(javaUtilConcurrent)
                .testEquals();
    }

    @Nested
    @DisplayName("obtain `Annotation`")
    class FindAnnotation {

        @Test
        @DisplayName("present directly in the package")
        void presentDirectly() {
            var pkg = Sub1Class.class.getPackage();
            var annotation = assertAnnotated(pkg);

            var packageInfo = PackageInfo.of(pkg);
            var optional = packageInfo.findAnnotation(ValueAnnotation.class);
            assertTrue(optional.isPresent());
            assertEquals(annotation, optional.get());
        }

        @Test
        @DisplayName("present in immediate parent package")
        void fromImmediateParent() {
            var pkg = Sub2Class.class.getPackage();
            assertNotAnnotated(pkg);
            assertFound(pkg);
        }

        @Test
        @DisplayName("present in a parent above")
        void fromParentAbove() {
            var pkg = Sub3Class.class.getPackage();
            assertNotAnnotated(pkg);
            assertFound(pkg);
        }

        private void assertFound(Package pkg) {
            var packageInfo = PackageInfo.of(pkg);
            var optional = packageInfo.findAnnotation(ValueAnnotation.class);
            assertTrue(optional.isPresent());
        }

        private ValueAnnotation assertAnnotated(Package pkg) {
            var annotation = pkg.getAnnotation(ValueAnnotation.class);
            // Make sure that the package is annotated in the test environment.
            assertNotNull(annotation);
            return annotation;
        }

    }

    @Test
    @DisplayName("tell if there is not annotation")
    void notFound() {
        var pkg = LastVisitor.class.getPackage();
        assertNotAnnotated(pkg);
        assertFalse(PackageInfo.of(pkg)
                               .findAnnotation(ValueAnnotation.class)
                               .isPresent());
    }

    private static void assertNotAnnotated(Package pkg) {
        var annotation = pkg.getAnnotation(ValueAnnotation.class);
        // Make sure that the package is NOT annotated in the test environment.
        assertNull(annotation);
    }
}
