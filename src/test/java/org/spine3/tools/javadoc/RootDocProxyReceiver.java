package org.spine3.tools.javadoc;

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
        final String name = RootDocProxyReceiver.class.getName();
        Main.execute(name, name, args);
    }

    @SuppressWarnings("unused") // called by com.sun.tools.javadoc.Main
    public static boolean start(RootDoc root) {
        final ExcludePrinciple excludePrinciple = new ExcludeInternalPrinciple(root);
        final ExcludeInternalDoclet doclet = new ExcludeInternalDoclet(excludePrinciple);

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
