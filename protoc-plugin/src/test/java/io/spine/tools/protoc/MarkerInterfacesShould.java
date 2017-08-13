/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.protoc;

import com.google.common.testing.NullPointerTester;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import org.junit.Test;

import javax.annotation.Generated;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Dashenkov
 */
public class MarkerInterfacesShould {

    @Test
    public void not_accept_nulls_on_construction() {
        new NullPointerTester().testAllPublicConstructors(MarkerInterfaces.class);
    }

    @Test
    public void not_accept_nulls() {
        new NullPointerTester().testAllPublicStaticMethods(MarkerInterfaces.class);
    }

    @Test
    public void generate_interfaces() {
        final String packageName = "io.spine.test";
        final String interfaceName = "CustomerEvent";
        final JavaFile javaFile = MarkerInterfaces.create(packageName, interfaceName);

        final AnnotationSpec generated = javaFile.typeSpec.annotations.get(0);
        assertEquals(Generated.class.getName(), generated.type.toString());

        assertEquals(packageName, javaFile.packageName);
        assertEquals(interfaceName, javaFile.typeSpec.name);
    }
}
