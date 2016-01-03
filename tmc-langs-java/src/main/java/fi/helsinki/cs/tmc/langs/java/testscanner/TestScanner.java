package fi.helsinki.cs.tmc.langs.java.testscanner;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.java.ClassPath;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class TestScanner {

    private static final Logger logger = LoggerFactory.getLogger(TestScanner.class);

    private final JavaCompiler compiler;
    private final StandardJavaFileManager fileManager;

    public TestScanner() {
        compiler = ToolProvider.getSystemJavaCompiler();
        fileManager = compiler.getStandardFileManager(null, null, null);
    }

    /**
     * Finds all tests for a given exercise.
     */
    public Optional<ExerciseDesc> findTests(
            ClassPath classPath, SourceFiles sourceFiles, String exerciseName) {
        if (sourceFiles.isEmpty()) {
            logger.warn("No testfiles available for exercise: {}.",  exerciseName);
            return Optional.absent();
        }

        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(classPath.toString());
        options.add("-proc:only");

        JavaCompiler.CompilationTask task =
                compiler.getTask(
                        null,
                        null,
                        null,
                        options,
                        null,
                        fileManager.getJavaFileObjectsFromFiles(sourceFiles.getSources()));

        TestMethodAnnotationProcessor processor = new TestMethodAnnotationProcessor();
        task.setProcessors(Collections.singletonList(processor));
        if (!task.call()) {
            throw new RuntimeException("Compilation failed");
        }

        return Optional.of(new ExerciseDesc(exerciseName, processor.getTestDescsSortedByName()));
    }
}
