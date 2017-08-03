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
package io.spine.tools.protobufjavadoc;

/**
 * @author Alexander Aleksandrov
 */
public class Given {
    private static final String SOURCE_FOLDER = "generated/main/java";
    private static final String DEBUG_OPTION = "--debug";
    private static final String testFile = "GeneratedJavaFile.java";
    private static final String COMPILE_LOG = ":compileJava";
    private static final String buildGradleFile = "build.gradle";

    public static String buildGradleFile() {
        return buildGradleFile;
    }

    public static String compileLog() {
        return COMPILE_LOG;
    }

    public static String testFile() {
        return testFile;
    }

    public static String debugOption() {
        return DEBUG_OPTION;
    }

    public static String sourceFolder() {
        return SOURCE_FOLDER;
    }
}
