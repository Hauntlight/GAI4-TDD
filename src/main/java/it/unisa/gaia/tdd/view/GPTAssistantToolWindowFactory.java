package it.unisa.gaia.tdd.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GPTAssistantToolWindowFactory implements ToolWindowFactory {

    public static final String TOOL_WINDOW_ID = "GAI4-TDD";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Content created dynamically
    }

    public static void showToolWindow(Project project, GPTAssistantToolWindowPanel toolWindowPanel) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);

        if (toolWindow == null) {
            // Should be registered in plugin.xml
            return;
        }

        toolWindowPanel.setP(project);

        // Replace existing content
        toolWindow.getContentManager().removeAllContents(true);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        toolWindow.activate(null);
    }
}