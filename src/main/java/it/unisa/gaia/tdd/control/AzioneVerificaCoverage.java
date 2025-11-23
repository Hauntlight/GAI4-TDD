package it.unisa.gaia.tdd.control;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import it.unisa.gaia.tdd.gai4settings;
import it.unisa.gaia.tdd.model.test.TestRunnerService;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AzioneVerificaCoverage extends AbstractAction {
    private final GPTAssistantToolWindowPanel parent;

    public AzioneVerificaCoverage(GPTAssistantToolWindowPanel parent) {
        super("Check Tests");
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = parent.getP();
        String testPath = parent.getTestPath();
        gai4settings settings = gai4settings.getInstance();
        if (settings.getAssistantModel().contains("codex")) {
            Messages.showErrorDialog("Optimized Green Phase is not supported for Codex models.", "Not Supported");
            return;
        }
        ProgressManager.getInstance().run(new Task.Modal(project, "Checking Tests...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    TestRunnerService.TestResult res = TestRunnerService.runTests(project, testPath);

                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (res.success) {
                            Messages.showInfoMessage("Test Suite Passed!\n" + res.output, "Test Result");
                        } else {
                            Messages.showErrorDialog("Test Suite Failed.\n\nError:\n" + res.errorMessage, "Test Result");
                        }
                    }, ModalityState.NON_MODAL);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}