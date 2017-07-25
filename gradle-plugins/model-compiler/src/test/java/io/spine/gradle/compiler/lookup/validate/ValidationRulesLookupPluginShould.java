package io.spine.gradle.compiler.lookup.validate;

import io.spine.gradle.compiler.GradleProject;
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

import static io.spine.gradle.TaskName.FIND_VALIDATION_RULES;
import static io.spine.gradle.compiler.Extension.getDefaultMainGenResDir;
import static io.spine.gradle.compiler.lookup.validate.ValidationRulesLookupPlugin.getValidationPropsFileName;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Grankin
 */
public class ValidationRulesLookupPluginShould {

    private static final String PROJECT_NAME = "validation-rules-lookup-plugin-test";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void findNestedValidationRules() throws Exception {
        final String file = "nested_validation_rule.proto";
        newGradleProject(file).executeTask(FIND_VALIDATION_RULES);
        assertTrue(getProperties().elements()
                                  .hasMoreElements());
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

    private GradleProject newGradleProject(String protoFileName) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(testProjectDir)
                            .addProtoFile(protoFileName)
                            .build();
    }
}
