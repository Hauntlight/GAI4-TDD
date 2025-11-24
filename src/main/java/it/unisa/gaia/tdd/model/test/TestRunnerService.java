package it.unisa.gaia.tdd.model.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TestRunnerService {

    public static class TestResult {
        public boolean success;
        public String errorMessage;
        public String output;
    }

    public static TestResult runTests(Project project, String testFilePath) throws IOException {
        // 1. Extract python runner from JAR to temp
        File runnerScript = File.createTempFile("gaitdd_runner", ".py");
        Files.copy(TestRunnerService.class.getResourceAsStream("/python/test_runner.py"), runnerScript.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // 2. Get the correct Python Executable (Venv or System)
        String pythonCmd = getProjectPythonInterpreter(project);
        String projectPath = project.getBasePath();

        // 3. Build Process
        // We pass the python executable path found in PyCharm settings
        ProcessBuilder pb = new ProcessBuilder(pythonCmd, runnerScript.getAbsolutePath(), testFilePath, projectPath);

        // Redirect Error stream to Output stream to capture everything
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n"); // Keep newlines for readability
            }
        }

        runnerScript.delete();

        ObjectMapper mapper = new ObjectMapper();
        TestResult res = new TestResult();

        try {
            // Attempt to parse the last line as JSON (the script prints JSON at the end)
            // We split by newline and take the last non-empty line because sometimes
            // libraries print warnings to stdout which pollute the output.
            String[] lines = output.toString().trim().split("\n");
            String jsonLine = lines[lines.length - 1];

            JsonNode root = mapper.readTree(jsonLine);
            res.success = root.path("success").asBoolean();
            res.errorMessage = root.path("error_message").asText();
            res.output = root.path("output").asText();
        } catch (Exception e) {
            // Fallback for parser errors (e.g., Python crashed before printing JSON)
            res.success = false;
            res.errorMessage = "Failed to parse Python Output.\nRaw Output:\n" + output.toString();
            res.output = output.toString();
        }

        return res;
    }

    /**
     * Recupera il percorso dell'interprete Python configurato nel progetto PyCharm.
     * Se non ne trova uno, prova i comandi di sistema standard.
     */
    private static String getProjectPythonInterpreter(Project project) {
        try {
            // Chiede al ProjectRootManager l'SDK attuale
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();

            if (projectSdk != null && projectSdk.getHomePath() != null) {
                // Ritorna il percorso completo (es: /home/user/progetto/venv/bin/python)
                return projectSdk.getHomePath();
            }
        } catch (Exception e) {
            // Ignora errori se l'SDK non è configurato o accessibile
        }

        // Fallback: Se non c'è un SDK configurato, prova il python di sistema
        return getSystemPythonFallback();
    }

    private static String getSystemPythonFallback() {
        try {
            Process p = Runtime.getRuntime().exec("python3 --version");
            if (p.waitFor() == 0) return "python3";
        } catch (Exception e) { /* ignore */ }
        return "python";
    }
}