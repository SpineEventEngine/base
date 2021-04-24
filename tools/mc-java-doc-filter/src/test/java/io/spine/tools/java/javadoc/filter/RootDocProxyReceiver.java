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

import com.sun.javadoc.RootDoc;
import com.sun.tools.javadoc.Main;

public class RootDocProxyReceiver extends ExcludeInternalDoclet {

    @SuppressWarnings("StaticVariableMayNotBeInitialized") // Used only start invocation
    private static RootDoc rootDocProxy;

    @SuppressWarnings("ConstantConditions") // Is not necessary
    public RootDocProxyReceiver(String[] args) {
        super(null);
        main(args);
    }

    public static void main(String[] args) {
        String name = RootDocProxyReceiver.class.getName();
        Main.execute(name, name, args);
    }

    @SuppressWarnings("unused") // called by com.sun.tools.javadoc.Main
    public static boolean start(RootDoc root) {
        ExcludePrinciple excludePrinciple = new ExcludeInternal(root);
        ExcludeInternalDoclet doclet = new ExcludeInternalDoclet(excludePrinciple);

        // We can obtain RootDoc only here
        rootDocProxy = (RootDoc) doclet.process(root, root.getClass());

        return true;
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization") // Initialized in start method
    static RootDoc rootDocFor(String[] args) {
        main(args);
        return rootDocProxy;
    }
}
