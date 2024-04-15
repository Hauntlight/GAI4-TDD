package it.unisa.gaia.tdd.view;

import javax.swing.JComponent;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class GPTAssistantToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    public static final String TOOL_WINDOW_ID = "GAI4-TDD";
    public static final String TOOL_WINDOW_TITLE = "TDD Assistant";

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        // Non è necessario creare il contenuto della finestra degli strumenti qui
        // Invece, il contenuto verrà creato dinamicamente durante la creazione della finestra degli strumenti
    }

    public static void showToolWindow(Project project, GPTAssistantToolWindowPanel toolWindowPanel) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

        // ID della finestra degli strumenti (assicurati che sia univoco)
        String toolWindowId = TOOL_WINDOW_ID;

        // Crea la finestra degli strumenti se non esiste già
        ToolWindow toolWindow = toolWindowManager.getToolWindow(toolWindowId);
        if (toolWindow == null) {
            toolWindow = toolWindowManager.registerToolWindow(toolWindowId, true, com.intellij.openapi.wm.ToolWindowAnchor.RIGHT);
            toolWindowPanel.setP(project);
        }

        // Ottieni il contenuto della finestra degli strumenti
        GPTAssistantToolWindowPanel existingPanel = getContentPanel(toolWindow, GPTAssistantToolWindowPanel.class);

        // Se il pannello esiste già, aggiorna il contenuto
        if (existingPanel != null) {
            existingPanel.updateContent(toolWindowPanel.classeDaCompletareTextField.getText(), project);
        } else {
            // Aggiungi il pannello alla finestra degli strumenti
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(toolWindowPanel, "", false);
            toolWindow.getContentManager().addContent(content);
        }

        // Attiva la finestra degli strumenti
        toolWindow.activate(null);
    }

    private static <T> T getContentPanel(ToolWindow toolWindow, Class<T> panelClass) {
        Content[] contents = toolWindow.getContentManager().getContents();
        for (Content content : contents) {
            JComponent component = content.getComponent();
            if (panelClass.isInstance(component)) {
                return panelClass.cast(component);
            }
        }
        return null;
    }
}
