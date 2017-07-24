package io.spine.gradle.compiler.lookup.validate;

import io.spine.gradle.compiler.ProjectConfigurator;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.spine.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.gradle.compiler.Extension.getDefaultMainGenResDir;
import static io.spine.gradle.compiler.lookup.validate.ValidationRulesLookupPlugin.getValidationPropsFileName;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Grankin
 */
public class ValidationRulesLookupPluginShould {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @SuppressWarnings("UseOfSystemOutOrSystemErr") // To print errors of a test Gradle build.
    @Test
    public void findNestedValidationRules() throws Exception {
        final ProjectConnection connection =
                new Configurator(testProjectDir, "nested_validation_rule.proto").configure();
        final BuildLauncher launcher = connection.newBuild();

        launcher.setStandardError(System.out)
                .forTasks(FIND_VALIDATION_RULES.getValue());
        try {
            launcher.run(new ResultHandler<Void>() {
                @Override
                public void onComplete(Void aVoid) {
                    assertTrue(getProperties().elements()
                                              .hasMoreElements());
                }

                @Override
                public void onFailure(GradleConnectionException e) {
                    throw e;
                }
            });
        } finally {
            connection.close();
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
    }

    private Dictionary getProperties() {
        final String projectPath = testProjectDir.getRoot()
                                                 .getAbsolutePath();
        final Path path = Paths.get(projectPath, getDefaultMainGenResDir(),
                                    getValidationPropsFileName());
        try {
            final InputStream inputStream = new FileInputStream(path.toFile());
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static class Configurator extends ProjectConfigurator {

        private final String protoFile;

        private Configurator(TemporaryFolder projectDirectory, String protoFile) {
            super("validation-rules-lookup-plugin-test", projectDirectory);
            this.protoFile = protoFile;
        }

        @Override
        public ProjectConnection configure() throws IOException {
            writeBuildGradle();
            writeProto(protoFile);
            return createProjectConnection();
        }
    }
}
