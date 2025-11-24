package it.unisa.gaia.tdd.control;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowFactory;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowPanel;

import java.io.File;

public class GPTAssistantAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile vFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getProject();

        if (vFile == null || project == null) {
            Messages.showMessageDialog("Choose a file", "Error", Messages.getErrorIcon());
            return;
        }

        String path = vFile.getPath().replace("/", File.separator);

        GPTAssistantToolWindowPanel panel = new GPTAssistantToolWindowPanel(path, project);
        GPTAssistantToolWindowFactory.showToolWindow(project, panel);
    }
}