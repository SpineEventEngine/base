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

package io.spine.tools.java.javadoc.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class JavadocArgsBuilder {

    @SuppressWarnings("AccessOfSystemProperties") // Need to know project path
    private static final String ABSOLUTE_PROJECT_PATH = System.getProperty("user.dir");
    private static final String TEST_SOURCES_DIR = ABSOLUTE_PROJECT_PATH + "/src/test/resources/testsources/";
    private static final String RESOURCES_DIR = ABSOLUTE_PROJECT_PATH + "/src/test/resources/";
    private static final String JAVADOC_OUTPUT_DIR = RESOURCES_DIR  + "javadocs";

    private final Collection<String> classes = new ArrayList<>();
    private final Collection<String> packages = new ArrayList<>();

    JavadocArgsBuilder addSource(String sourceName) {
        classes.add(TEST_SOURCES_DIR + sourceName);
        return this;
    }

    JavadocArgsBuilder addPackage(String packageName) {
        packages.add(packageName);
        return this;
    }

    String[] build() {
        List<String> allArguments = new ArrayList<>();

        addDestination(allArguments);
        addSourcePath(allArguments);
        allArguments.addAll(packages);
        allArguments.addAll(classes);

        return allArguments.toArray(new String[allArguments.size()]);
    }

    private static void addSourcePath(Collection<String> commandLineArgs) {
        // Path to scan packages
        commandLineArgs.add("-sourcepath");
        commandLineArgs.add(RESOURCES_DIR);
    }

    private static void addDestination(Collection<String> commandLineArgs) {
        commandLineArgs.add("-d");
        commandLineArgs.add(JAVADOC_OUTPUT_DIR);
    }

    static String getJavadocDir() {
        return JAVADOC_OUTPUT_DIR;
    }
}
