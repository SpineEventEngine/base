/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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
package org.spine3.tools.codestyle.javadoc.link;

import com.google.common.base.Optional;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.gradle.SpinePlugin;
import org.spine3.tools.codestyle.Extension;
import org.spine3.tools.codestyle.Response;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.compile;
import static org.spine3.gradle.TaskName.CHECK_FQN;
import static org.spine3.gradle.TaskName.COMPILE_JAVA;
import static org.spine3.gradle.TaskName.PROCESS_RESOURCES;

/**
 * The plugin that checks the target project Javadocs for broken links that
 * are stated in the wrong format.
 *
 * @author Alexander Aleksandrov
 */
public class JavadocLinkCheckerPlugin extends SpinePlugin {

    private static final String DIRECTORY_TO_CHECK = "/src/main/java";
    private static final String JAVA_EXTENSION = ".java";
    private static final InvalidResultStorage storage = new InvalidResultStorage();

    /**
     * The quantity of broken link that will make an exception.
     */
    private int exceptionThreshold = 0;

    /**
     * The behavior that can be either warning or error.
     *
     * {@link Response#WARN} is default.
     */
    private Response responseType = Response.WARN;

    @Override
    public void apply(Project project) {
        final Action<Task> action = actionFor(project);
        newTask(CHECK_FQN, action).insertAfterTask(COMPILE_JAVA)
                                  .insertBeforeTask(PROCESS_RESOURCES)
                                  .applyNowTo(project);
        log().debug("Starting to check Javadocs {}", action);
    }

    public Action<Task> actionFor(final Project project) {
        log().debug("Preparing an action for the Javadock checker");
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                exceptionThreshold = Extension.getThreshold(project);
                final String type = Extension.getResponseType(project)
                                             .trim()
                                             .toUpperCase();
                responseType = Response.valueOf(type);

                final List<String> dirsToCheck = getDirsToCheck(project);
                findFqnLinksWithoutText(dirsToCheck);
                log().debug("Ending an action");
            }
        };
    }

    private static List<String> getDirsToCheck(Project project) {
        log().debug("Finding the directories to check");
        final String mainScopeJavaFolder = project.getProjectDir()
                                                  .getAbsolutePath() + DIRECTORY_TO_CHECK;
        final List<String> result = newArrayList(mainScopeJavaFolder);
        log().debug("{} directories found for the check: {}", result.size(), result);

        return result;
    }

    private void findFqnLinksWithoutText(List<String> pathsToDirs) {
        for (String path : pathsToDirs) {
            final File file = new File(path);
            if (file.exists()) {
                checkRecursively(file.toPath());
            } else {
                log().debug("No more files left to check");
            }
        }
    }

    private void checkRecursively(Path path) {
        try {
            final FileVisitor<Path> visitor = new RecursiveFileChecker();
            log().debug("Starting to check the files recursively in {}", path.toString());
            Files.walkFileTree(path, visitor);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to check the folder with its contents: " +
                                                    path, e);
        }
    }

    /**
     * Custom {@linkplain java.nio.file.FileVisitor visitor} which recursively checks
     * the contents of the walked folder.
     */
    private class RecursiveFileChecker extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            super.visitFile(path, attrs);
            log().debug("Performing FQN check for the file: {}", path);
            check(path);
            return FileVisitResult.CONTINUE;
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            log().error("Error walking down the file tree for file: {}", file);
            return FileVisitResult.TERMINATE;
        }
    }

    private void check(Path path) throws InvalidFqnUsageException {
        final List<String> content;
        if (!path.toString()
                 .endsWith(JAVA_EXTENSION)) {
            return;
        }
        try {
            content = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the contents of the file: " + path, e);
        }
        final List<InvalidFqnUsage> invalidLinks = check(content);

        if (!invalidLinks.isEmpty()) {
            storage.save(path, invalidLinks);
            if (storage.getLinkTotal() > exceptionThreshold) {
                storage.logInvalidFqnUsages();
                responseType.logOrFail(new InvalidFqnUsageException());
            }
        }
    }

    private static List<InvalidFqnUsage> check(List<String> content) {
        int lineNumber = 0;
        final List<InvalidFqnUsage> invalidLinks = newArrayList();
        for (String line : content) {
            final Optional<InvalidFqnUsage> result = checkSingleComment(line);
            lineNumber++;
            if (result.isPresent()) {
                final InvalidFqnUsage invalidFqnUsage = result.get();
                invalidFqnUsage.setIndex(lineNumber);
                invalidLinks.add(invalidFqnUsage);
            }
        }
        return invalidLinks;
    }

    private static Optional<InvalidFqnUsage> checkSingleComment(String comment) {
        final Matcher matcher = JavadocLinkCheckerPlugin.JavadocPattern.LINK.getPattern()
                                                                            .matcher(comment);
        final boolean found = matcher.find();
        if (found) {
            final String improperUsage = matcher.group(0);
            final InvalidFqnUsage result = new InvalidFqnUsage(improperUsage);
            return Optional.of(result);
        }
        return Optional.absent();
    }

    private enum JavadocPattern {

        /*
         * This regexp matches every link or linkplain in javadoc that is not in the format of
         * {@link <FQN> <text>} or {@linkplain <FQN> <text>}.
         *
         * Wrong links: {@link org.spine3.base.Client} or {@linkplain com.guava.AnyClass }
         * Correct linsk: {@link Class.InternalClass}, {@link org.spine3.base.Client Client},
         * {@linkplain org.spine3.base.Client some client class}
         *
         * 1st Capturing Group "(\{@link|\{@linkplain)"
         * 1st Alternative "\{@link"
         * "\{" matches the character "{" literally (case sensitive)
         * "@link" matches the characters "@link" literally (case sensitive)
         * 2nd Alternative "\{@linkplain"
         * "\{" matches the character "{" literally (case sensitive)
         * "@linkplain" matches the characters "@linkplain" literally (case sensitive)
         * " *" matches the character " " literally (case sensitive)
         * "*" Quantifier — Matches between zero and unlimited times, as many times as possible,
         * giving back as needed (greedy)

         * 2nd Capturing Group "((?!-)[a-z0-9-]{1,63}\.)"
         * Negative Lookahead "(?!-)"
         * Assert that the Regex below does not match
         * "-"matches the character "-" literally (case sensitive)
         * Match a single character present in the list below "[a-z0-9-]{1,63}"
         * "{1,63}" Quantifier — Matches between 1 and 63 times, as many times as possible,
         * giving back as needed (greedy)
         * "a-z" a single character in the range between "a" (ASCII 97) and "z" (ASCII 122)
         * (case sensitive)
         * "0-9" a single character in the range between "0" (ASCII 48) and "9" (ASCII 57)
         * (case sensitive)
         * "-" matches the character "-" literally (case sensitive)
         * "\." matches the character "." literally (case sensitive)

         * 3rd Capturing Group "((?!-)[a-zA-Z0-9-]{1,63}[a-zA-Z0-9-]\.)+"
         * "+" Quantifier — Matches between one and unlimited times, as many times as possible,
         * giving back as needed (greedy)
         * A repeated capturing group will only capture the last iteration.
         * Put a capturing group around the repeated group to capture all iterations or use a
         * non-capturing group instead if you're not interested in the data.

         * 4th Capturing Group "(\}|\ *\})"
         * 1st Alternative "\}"
         * "\}" matches the character "}" literally (case sensitive)
         * 2nd Alternative "\ *\}"
         * "\ *" matches the character " " literally (case sensitive)
         *  "*" Quantifier — Matches between zero and unlimited times, as many times as possible,
         *  giving back as needed (greedy)
         * "\}" matches the character "}" literally (case sensitive)
         */
        LINK(compile("(\\{@link|\\{@linkplain) *((?!-)[a-z0-9-]{1,63}\\.)((?!-)[a-zA-Z0-9-]{1,63}[a-zA-Z0-9-]\\.)+[a-zA-Z]{2,63}(\\}|\\ *\\})"));

        private final Pattern pattern;

        JavadocPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    private static Logger log() {
        return JavadocLinkCheckerPlugin.LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(JavadocLinkCheckerPlugin.class);
    }

}
