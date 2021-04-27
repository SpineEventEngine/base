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

package io.spine.tools.mc.java.gradle.check;

import org.gradle.api.Project;

/**
 * The Error Prone Checks plugin extension.
 *
 * <p>Allows configuring severity for all the Spine-custom Error Prone checks applied to the
 * project.
 *
 * @see Severity
 */
@SuppressWarnings("PublicField" /* required for exposing the property in Gradle. */)
public class Extension {

    private static final String NAME = "modelChecks";

    /**
     * The default severity of the model checks.
     *
     * <p>If this value is not set, default severity of each check are used.
     *
     * @see com.google.errorprone.BugPattern.SeverityLevel
     */
    public Severity defaultSeverity;

    /**
     * The severity of the {@code useValidatingBuilder} check.
     *
     * @see io.spine.tools.mc.java.check.vbuild.UseVBuild
     */
    public Severity useValidatingBuilder;

    public static String name() {
        return NAME;
    }

    static Extension of(Project project) {
        Extension extension = (Extension)
                project.getExtensions()
                       .getByName(name());
        return extension;
    }

    public static Severity getUseValidatingBuilder(Project project) {
        Extension extension = of(project);
        return extension.useValidatingBuilder;
    }
}
