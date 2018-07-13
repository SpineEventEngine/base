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

package io.spine.tools.compiler.rejection;

import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.Main;
import org.junit.rules.TemporaryFolder;

/**
 * {@link RootDoc} receiver for the tests purposes.
 *
 * <p>To receive {@link RootDoc}, necessary extend {@link Standard} doclet
 * and define static method with the following signature:
 * <pre>{@code public static boolean start(RootDoc}</pre>
 *
 * @author Dmytro Grankin
 */
@SuppressWarnings("ExtendsUtilityClass")
public class RootDocReceiver extends Standard {

    /**
     * Should be received only by {@link #getRootDoc(TemporaryFolder, String)} call,
     * that guarantees proper initialization.
     */
    @SuppressWarnings("StaticVariableMayNotBeInitialized")
    private static RootDoc rootDoc;

    /**
     * Returns {@link RootDoc} for the specified source file.
     *
     * <p>Executes {@link #main(String[])}, which in turn
     * calls {@link Main#execute(String, String, String...)}.
     * Such a call chain guarantees a proper {@link #rootDoc} initialization.
     *
     * @param projectDir     the project directory, that contains the source
     * @param sourceLocation the source relative location
     * @return the root document
     */
    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    public static RootDoc getRootDoc(TemporaryFolder projectDir, String sourceLocation) {
        main(new String[]{
                projectDir.getRoot()
                          .getAbsolutePath() + sourceLocation
        });
        return rootDoc;
    }

    public static void main(String[] args) {
        String name = RootDocReceiver.class.getName();
        Main.execute(name, name, args);
    }

    /**
     * Receives the {@link RootDoc} and initializes {@link #rootDoc}.
     *
     * <p>Always returns {@code true} to let the doclet processor know there were no issues.
     *
     * <p>Called by {@link Main#execute(String, String, String...)}
     *
     * @param root the {@link RootDoc} formed by {@link Main#execute(String, String, String...)}
     * @return {@code true} anyway
     */
    @SuppressWarnings("unused")
    public static boolean start(RootDoc root) {
        rootDoc = root;
        return true;
    }
}
