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

import com.google.common.io.Files;
import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
public class MarkerInterfaceGeneratorShould {

    private static final Path genFolder = Files.createTempDir().toPath();

    @Test
    public void not_accept_nulls_on_construction() {
        new NullPointerTester().testAllPublicConstructors(MarkerInterfaceGenerator.class);
    }

    @Test
    public void not_accept_nulls() {
        final MarkerInterfaceGenerator generator = new MarkerInterfaceGenerator(genFolder);
        new NullPointerTester().testAllPublicInstanceMethods(generator);
    }

    @Test
    public void generate_interfaces() {
        final String packageName = "io.spine.test";
        final String interfaceName = "CustomerEvent";
        final MarkerInterfaceGenerator generator = new MarkerInterfaceGenerator(genFolder);
        generator.generate(packageName, interfaceName);
        final String generatedClassPath = packageName.replace('.', '/') + '/' + interfaceName + ".java";
        final Path interfacePath = genFolder.resolve(generatedClassPath);
        final File interfaceFile = interfacePath.toFile();
        assertTrue(interfaceFile.exists());
    }
}
