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

package io.spine.tools.javadoc;

import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;
import com.sun.tools.javadoc.Main;
import com.sun.tools.javadoc.MethodDocImpl;
import io.spine.annotation.Internal;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * Extension of {@linkplain Standard} doclet, which excludes
 * {@linkplain Internal}-annotated components.
 *
 * <p>Use it to generate documentation for audience, that should not know about
 * {@linkplain Internal}-annotated components.
 *
 * <p>Works by pre-processing a {@linkplain RootDoc}.
 * The doclet creates new {@linkplain RootDoc},
 * that does not contain {@linkplain Internal}-annotated components and further generates documents.
 *
 * <p>You can use the non-standard doclet by specifying the following Javadoc options:
 * <ul>
 *     <li>doclet io.spine.tools.javadoc.ExcludeInternalDoclet;</li>
 *     <li>docletpath classpathlist (The path to the doclet starting class file).</li>
 * </ul>
 *
 * <p>Call it with Javadoc tool like this:
 * <pre> {@code javadoc -doclet io.spine.tools.javadoc.ExcludeInternalDoclet
 * -docletpath "classpathlist" ...}</pre>
 *
 * <p>If everything done right, you will get the standard documentation generated by Javadoc tool,
 * except {@linkplain Internal}-annotated components.
 */
@SuppressWarnings("ExtendsUtilityClass")
public class ExcludeInternalDoclet extends Standard {

    private final ExcludePrinciple excludePrinciple;

    ExcludeInternalDoclet(ExcludePrinciple excludePrinciple) {
        super();
        this.excludePrinciple = excludePrinciple;
    }

    /**
     * Entry point for the Javadoc tool.
     *
     * @param args the command-line parameters
     */
    public static void main(String[] args) {
        String name = ExcludeInternalDoclet.class.getName();
        Main.execute(name, name, args);
    }

    /**
     * The "start" method as required by Javadoc.
     *
     * @param root the root of the documentation tree
     * @return {@code true} if the doclet ran without encountering any errors,
     * {@code false} otherwise
     */
    @SuppressWarnings({"unused", "RedundantSuppression"}) // called by com.sun.tools.javadoc.Main
    public static boolean start(RootDoc root) {
        ExcludePrinciple excludePrinciple = new ExcludeInternalPrinciple(root);
        ExcludeInternalDoclet doclet = new ExcludeInternalDoclet(excludePrinciple);
        return Standard.start((RootDoc) doclet.process(root, RootDoc.class));
    }

    /**
     * Creates proxy of "com.sun..." interfaces and excludes
     * {@linkplain ProgramElementDoc}s using {@linkplain #excludePrinciple}.
     *
     * @param returnValue the value to process
     * @param returnValueType the expected type of value
     * @return the processed value
     */
    @Nullable
    Object process(@Nullable Object returnValue, Class returnValueType) {
        if (returnValue == null) {
            return null;
        }

        if (returnValue.getClass()
                       .getName()
                       .startsWith("com.sun.")) {
            Class cls = returnValue.getClass();
            return Proxy.newProxyInstance(cls.getClassLoader(),
                                          cls.getInterfaces(),
                                          new ExcludeHandler(returnValue));
        } else if (returnValue instanceof Object[] && returnValueType.getComponentType() != null) {
            Class componentType = returnValueType.getComponentType();
            Object[] array = (Object[]) returnValue;
            List<Object> list = new ArrayList<>();
            for (Object entry : array) {
                if (!(entry instanceof ProgramElementDoc && excludePrinciple.shouldExclude(
                        (ProgramElementDoc) entry))) {
                    list.add(process(entry, componentType));
                }
            }
            return list.toArray((Object[]) Array.newInstance(componentType, list.size()));
        } else {
            return returnValue;
        }
    }

    /**
     * The {@linkplain InvocationHandler} for the "com.sun..." proxies.
     */
    private class ExcludeHandler implements InvocationHandler {

        private final Object target;

        private ExcludeHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args != null && IgnoredMethod.isIgnored(method.getName())) {
                args[0] = unwrap(args[0]);
            }
            try {
                return process(method.invoke(target, args), method.getReturnType());
            } catch (InvocationTargetException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        private Object unwrap(Object proxy) {
            if (proxy instanceof Proxy) {
                return ((ExcludeHandler) Proxy.getInvocationHandler(proxy)).target;
            }
            return proxy;
        }
    }

    /**
     * Enumeration of method names used in {@linkplain Standard} doclet implementation,
     * that cast parameter represented by interface to concrete implementation type.
     *
     * <p>For example {@linkplain MethodDocImpl#overrides(MethodDoc)}:
     * <pre> {@code
     *   public boolean overrides(MethodDoc meth) {
     *       MethodSymbol overridee = ((MethodDocImpl) meth).sym;
     *       // Remaining part omitted.
     *   }
     * }</pre>
     *
     * <p>Because we use proxy to filter Javadocs, we should unwrap proxy
     * of parameters passed to these methods to prevent {@code ClassCastException}.
     */
    @SuppressWarnings({"unused", "RedundantSuppression"}) // Used in implicit form.
    private enum IgnoredMethod {
        COMPARE_TO("compareTo"),
        EQUALS("equals"),
        OVERRIDES("overrides"),
        SUBCLASS_OF("subclassOf");

        private final String methodName;

        IgnoredMethod(String methodName) {
            this.methodName = methodName;
        }

        String getMethodName() {
            return methodName;
        }

        /**
         * Returns {@code true} if the passed method name is one of {@code IgnoredMethod}s.
         *
         * @param methodName the method name to test
         * @return {@code true} if the method name is one of {@code IgnoredMethod}s
         */
        private static boolean isIgnored(String methodName) {
            for (IgnoredMethod ignoredMethod : IgnoredMethod.values()) {
                if (methodName.equals(ignoredMethod.getMethodName())) {
                    return true;
                }
            }

            return false;
        }
    }
}
