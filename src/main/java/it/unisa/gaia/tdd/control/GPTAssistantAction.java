package it.unisa.gaia.tdd.control;

import java.io.File;

import javax.swing.JComponent;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;

import it.unisa.gaia.tdd.view.GPTAssistantToolWindowFactory;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowPanel;


//Questa Azione Genera viene eseguita quando l'utente utilizza GAI4

public class GPTAssistantAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent e) {
		// Ottieni il percorso assoluto del file selezionato
		// TODO try catch
		String filePath;
		try {
			filePath = e.getDataContext().getData("virtualFile").toString();
		} catch (Exception ex) {
			Messages.showMessageDialog("Choose a file", "ERROR!!",
					Messages.getInformationIcon());
			return;
		}
		 // Rimuovi "file://"
        String cleanedUrl = filePath.replace("file://", "");

        // Sostituisci tutti i "/" con "\"
        String finalPath = cleanedUrl.replace("/", File.separator);

		showToolWindow(finalPath, e.getProject());
	}

	private void showToolWindow(String filePath, Project project) {
		GPTAssistantToolWindowPanel toolWindowPanel = new GPTAssistantToolWindowPanel(filePath, project);
		GPTAssistantToolWindowFactory.showToolWindow(project, toolWindowPanel);

	}
}
