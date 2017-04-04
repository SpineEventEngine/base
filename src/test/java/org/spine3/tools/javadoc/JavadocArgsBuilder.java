package org.spine3.tools.javadoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class JavadocArgsBuilder {

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
        final List<String> allArguments = new ArrayList<>();

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
