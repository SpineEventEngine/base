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

package io.spine.tools;

import java.util.Locale;

/**
 * Detects current operating system properties.
 *
 * <p>Based on {@code org.apache.tools.ant.taskdefs.condition.Os}.
 */
@SuppressWarnings("AccessOfSystemProperties") // to get current OS props.
public enum OsFamily {

    Windows,

    Win9x {
        @Override
        public boolean isCurrent() {
            boolean hasSuffix = OS_NAME.contains("95")
                    || OS_NAME.contains("98")
                    || OS_NAME.contains("me");
            return Windows.isCurrent() && hasSuffix;
        }
    },

    WinNT {
        @Override
        public boolean isCurrent() {
            return Windows.isCurrent() && !Win9x.isCurrent();
        }
    },

    Os2("os/2"),

    macOS("mac") {
        @Override
        public boolean isCurrent() {
            return super.isCurrent() || OS_NAME.contains(DARWIN);
        }
    },

    OpenVms("openvms"),

    Unix {
        @Override
        public boolean isCurrent() {
            boolean separatorMatches = PATH_SEP.equals(":");
            boolean notMac = !macOS.isCurrent()
                    || OS_NAME.endsWith("x")
                    || OS_NAME.contains(DARWIN);
            boolean notVms = !OpenVms.isCurrent();
            return separatorMatches && notVms && notMac;
        }
    };

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final String PATH_SEP = System.getProperty("path.separator");

    /**
     * OpenJDK is reported to call MacOS X "Darwin".
     *
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=44889">Bug 1</a>
     * @see <a href="https://issues.apache.org/jira/browse/HADOOP-3318">Bug 2</a>
     */
    private static final String DARWIN = "darwin";

    private final String value;

    OsFamily() {
        this.value = name().toLowerCase(Locale.ENGLISH);
    }

    OsFamily(String name) {
        this.value = name;
    }

    private String value() {
        return value;
    }

    public boolean isCurrent() {
        boolean result = OS_NAME.contains(value());
        return result;
    }
}
