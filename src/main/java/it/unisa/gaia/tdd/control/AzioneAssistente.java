package it.unisa.gaia.tdd.control;

import com.intellij.diff.contents.DocumentContent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import it.unisa.gaia.tdd.gai4settings;
import it.unisa.gaia.tdd.model.llm.*;
import it.unisa.gaia.tdd.model.test.TestRunnerService;
import it.unisa.gaia.tdd.view.CodeDiffDialog;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AzioneAssistente extends AbstractAction {
    private static final Logger LOGGER = Logger.getInstance(AzioneAssistente.class);
    private final GPTAssistantToolWindowPanel parent;

    public AzioneAssistente(GPTAssistantToolWindowPanel parent) {
        super("Green Phase");
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = parent.getP();
        String classPath = parent.getPath();
        String testPath = parent.getTestPath();

        if (classPath.isEmpty() || testPath.isEmpty()) {
            Messages.showErrorDialog("Please select both class and test files.", "Missing Files");
            return;
        }

        ProgressManager.getInstance().run(new Task.Modal(project, "Running GAI4-TDD Cycle", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("Running initial tests...");
                    TestRunnerService.TestResult testRes = TestRunnerService.runTests(project, testPath);

                    // If tests already pass, no need to fix
                    if (testRes.success) {
                        ApplicationManager.getApplication().invokeLater(() ->
                                Messages.showInfoMessage("Tests are already passing!", "Success"), ModalityState.NON_MODAL);
                        return;
                    }

                    indicator.setText("Asking AI for solution...");
                    String classContent = new String(Files.readAllBytes(Paths.get(classPath)));
                    String testContent = new String(Files.readAllBytes(Paths.get(testPath)));

                    LLMClient client = createClient();
                    String newCode = client.ask(classContent, testContent, testRes.errorMessage);

                    indicator.setText("Preparing diff view...");

                    ApplicationManager.getApplication().invokeLater(() -> {
                        showDiffDialog(project, classPath, classContent, newCode);
                    }, ModalityState.NON_MODAL);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showErrorDialog("Error: " + ex.getMessage(), "Execution Failed"), ModalityState.NON_MODAL);
                }
            }
        });
    }

    protected LLMClient createClient() {
        gai4settings settings = gai4settings.getInstance();
        String model = settings.getAssistantModel().toLowerCase();
        String apiKey = settings.getApiKey();

        if (model.contains("codex")) {
            return new OpenAIGPT51CodexClient(apiKey, settings.getAssistantModel());
        } else if (model.contains("gpt-5")) {
            return new OpenAIGPT5Client(apiKey, settings.getAssistantModel());
        } else if (model.contains("gpt")) {
            return new OpenAIClient(apiKey, settings.getAssistantModel());
        } else if (model.contains("claude")) {
            return new ClaudeClient(apiKey,settings.getAssistantModel());
        }else{
            return new ExternalClient(settings.getServer(), settings.getServerKey());
        }
    }

    private void showDiffDialog(Project project, String path, String oldContent, String newContent) {
        CodeDiffDialog dialog = new CodeDiffDialog(project, oldContent, newContent);
        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            DocumentContent modifiedContent = dialog.getContent();
            String finalCode = modifiedContent.getDocument().getText();

            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
                    if (file != null) {
                        Document doc = FileDocumentManager.getInstance().getDocument(file);
                        if (doc != null) doc.setText(finalCode);
                        file.refresh(false, true);
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            });
        }
    }
}