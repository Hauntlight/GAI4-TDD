package it.unisa.gaia.tdd.control;

import com.intellij.diff.contents.DocumentContent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
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
import it.unisa.gaia.tdd.model.llm.ExternalClient;
import it.unisa.gaia.tdd.model.llm.LLMClient;
import it.unisa.gaia.tdd.model.llm.OpenAIClient;
import it.unisa.gaia.tdd.model.llm.OpenAIGPT5Client;
import it.unisa.gaia.tdd.view.CodeDiffDialog;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AzioneRefactor extends AbstractAction {
    private final GPTAssistantToolWindowPanel parent;

    public AzioneRefactor(GPTAssistantToolWindowPanel parent) {
        super("Refactor");
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = parent.getP();
        String classPath = parent.getPath();
        String testPath = parent.getTestPath();

        gai4settings settings = gai4settings.getInstance();
        if (settings.getAssistantModel().contains("codex")) {
            Messages.showErrorDialog("Refactoring is not supported for Codex models.", "Not Supported");
            return;
        }

        ProgressManager.getInstance().run(new Task.Modal(project, "Refactoring...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    String originalCode = new String(Files.readAllBytes(Paths.get(classPath)));
                    String testCode = new String(Files.readAllBytes(Paths.get(testPath)));

                    LLMClient client = createClient();
                    String refactoredCode = client.refactor(originalCode, testCode);

                    ApplicationManager.getApplication().invokeLater(() -> {
                        showDiffDialog(project, classPath, originalCode, refactoredCode);
                    }, ModalityState.NON_MODAL);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private LLMClient createClient() {
        gai4settings settings = gai4settings.getInstance();
        String model = settings.getAssistantModel().toLowerCase();
        String apiKey = settings.getApiKey();

        if (model.contains("gpt-5")) {
            return new OpenAIGPT5Client(apiKey, settings.getAssistantModel());
        } else if (model.contains("gpt")) {
            return new OpenAIClient(apiKey, settings.getAssistantModel());
        } else {
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
                        FileDocumentManager.getInstance().getDocument(file).setText(finalCode);
                        file.refresh(false, true);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });
        }
    }
}