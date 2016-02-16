package fi.helsinki.cs.tmc.langs.java.ant;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.domain.ValueObject;
import fi.helsinki.cs.tmc.langs.java.ClassPath;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the argument list needed to run TMC TestRunner.
 */
public final class TestRunnerArgumentBuilder {

    private static final String JAVA_RUNTIME = "java";
    private static final String TEST_DIRECTORY_PARAM_PREFIX = "-Dtmc.test_class_dir=";
    private static final String RESULT_FILE_PARAM_PREFIX = "-Dtmc.results_file=";
    private static final String ENDORSED_LIBS_PARAM_PREFIX = "-Djava.endorsed.dirs=";
    private static final String CLASSPATH_PARAM_PREFIX = "-cp";
    private static final String RUNNER_MAIN_CLASS = "fi.helsinki.cs.tmc.testrunner.Main";

    private List<String> arguments;

    /**
     * Create TestRunnerArguments with all the necessary information for creating an argument list.
     */
    public TestRunnerArgumentBuilder(
            ValueObject runtimeArguments,
            Path projectBasePath,
            Path testDirectory,
            Path resultFile,
            ClassPath classPath,
            ExerciseDesc exercise) {
        arguments = new ArrayList<>();
        arguments.add(JAVA_RUNTIME);
        if (runtimeArguments != null) {
            arguments.add(runtimeArguments.asString());
        }
        arguments.add(TEST_DIRECTORY_PARAM_PREFIX + testDirectory.toString());
        arguments.add(RESULT_FILE_PARAM_PREFIX + resultFile.toString());

        if (endorsedLibsExists(projectBasePath)) {
            arguments.add(ENDORSED_LIBS_PARAM_PREFIX + createEndorsedLibsPath(projectBasePath));
        }

        arguments.add(CLASSPATH_PARAM_PREFIX);
        arguments.add(classPath.toString());
        arguments.add(RUNNER_MAIN_CLASS);
        for (String testCaseArgument : createTestCaseArgumentList(exercise)) {
            arguments.add(testCaseArgument);
        }
    }

    private boolean endorsedLibsExists(Path path) {
        File endorsedDir = createEndorsedLibsPath(path).toFile();
        return endorsedDir.exists() && endorsedDir.isDirectory();
    }

    private Path createEndorsedLibsPath(Path projectBasePath) {
        return Paths.get(projectBasePath.toString(), "lib", "endorsed");
    }

    private List<String> createTestCaseArgumentList(ExerciseDesc exercise) {
        List<String> testCases = new ArrayList<>();

        for (TestDesc desc : exercise.tests) {
            StringBuilder sb = new StringBuilder();
            sb.append(desc.name.replace(" ", "."));
            sb.append("{");
            for (int i = 0; i < desc.points.size(); i++) {
                sb.append(desc.points.get(i));
                if (i < desc.points.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("}");

            testCases.add(sb.toString());
        }

        return testCases;
    }

    /**
     * Get the arguments as a list of Strings, usable with ProcessBuilder.
     */
    public List<String> getArguments() {
        return arguments;
    }
}
