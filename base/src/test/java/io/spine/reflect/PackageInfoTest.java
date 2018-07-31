/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PackageInfo should")
class PackageInfoTest {

    private final PackageInfo javaUtil = PackageInfo.of(Collection.class.getPackage());
    private final PackageInfo javaUtilConcurrent = PackageInfo.of(Callable.class.getPackage());

    @Test
    @DisplayName("return package name in toString()")
    void stringify() {
        assertEquals(javaUtil.getValue()
                             .getName(),
                     javaUtil.toString());
    }

    @Test
    @DisplayName("have equals() and hashCode()")
    void hashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(javaUtil, PackageInfo.of(Collection.class.getPackage()))
                .addEqualityGroup(javaUtilConcurrent)
                .testEquals();
    }
}
