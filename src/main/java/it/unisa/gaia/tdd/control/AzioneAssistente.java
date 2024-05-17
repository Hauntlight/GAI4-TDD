package it.unisa.gaia.tdd.control;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.intellij.diff.contents.DiffContent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;

import it.unisa.gaia.tdd.gai4settings;
import it.unisa.gaia.tdd.view.CodeDialog;
import it.unisa.gaia.tdd.view.CodeDiffDialog;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowPanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AzioneAssistente extends AbstractAction {

	private GPTAssistantToolWindowPanel parent;
	private String output = "";

	public GPTAssistantToolWindowPanel getParent() {
		return parent;
	}

	public void setParent(GPTAssistantToolWindowPanel parent) {
		this.parent = parent;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public AzioneAssistente(GPTAssistantToolWindowPanel parent) {
		super();
		this.parent = parent;
		this.putValue(NAME, "RUN");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String key = gai4settings.getInstance().getApiKey();
		String parameters = "-k " + key + " " + parent.getParameters();
		//String externalParameters = parent.getExternalParameters();
		String server =  gai4settings.getInstance().getServer();
		String exKey = gai4settings.getInstance().getServerKey();
		String model = gai4settings.getInstance().getAssistantModel();
		if (model.trim().contains("gpt")){
			ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
				executeGPTScript(parameters + " -m "+model);
			}, "Running Script", false, parent.getP());
		}else{
			ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
				String externalP = "-s "+server +" -k " + exKey + " " + parent.getParameters();
				executeExternalScript(externalP);
			}, "Running Script", false, parent.getP());
		}
	}

	public DialogBuilder showBlockingPopup(@NotNull Project project) {
		DialogBuilder builder = new DialogBuilder(project);
		builder.setTitle("Wait");
		builder.setCenterPanel(new JLabel("Work in progress"));
		builder.setOkOperation(() -> {
		});

		builder.setCancelOperation(() -> {
		});
		builder.show();
		return builder;
	}

	private void executeGPTScript(String parameters) {
		File scriptFile = null;
		try {

			this.setEnabled(false);
			String tempDir = System.getProperty("java.io.tmpdir");
			scriptFile = new File(tempDir, "script_GPT_TDD.py");
			Files.copy(getClass().getResourceAsStream("/scripts/script_GPT_TDD.py"), scriptFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Project p = parent.getP();
			String command = "python " + scriptFile.getAbsolutePath() + " " + parameters + " -p "
					+'"'+parent.getP().getBasePath() + File.separator+"*"+'"';
			Process process = Runtime.getRuntime().exec(command);
			// Leggi l'output dello script Python
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			StringBuilder errorOutput = new StringBuilder();

			
			StringBuilder output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			
			String lineError;
			while ((lineError = readerError.readLine()) != null) {
			    errorOutput.append(lineError).append("\n");
			}

			readerError.close();

			JFrame mainFrame = WindowManager.getInstance().getFrame(parent.getP());
			mainFrame.setCursor(Cursor.getDefaultCursor());

			ApplicationManager.getApplication().invokeLater(()->{
				//Old View Implementation
				/*CodeDialog dialog = new CodeDialog(parent.getP(), "\n" + output);
	            dialog.show();*/
				//New View
				
				String content = "ERROR";
				try {
					 content = new String(Files.readAllBytes(Paths.get(parent.getPath())));
				} catch (IOException e) {
					e.printStackTrace();
				}

				CodeDiffDialog dialog = new CodeDiffDialog(parent.getP(),content,output.toString());
				DocumentContent contentModified = dialog.getContent();
				
				dialog.show();

	            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
	                String path = this.getParent().getPath();
	                
	                sovrascriviFile(parent.getP(), path, contentModified.getDocument().getText());
	                refreshFile(parent.getP(), path);
	            } else {
	                // L'utente ha cliccato su "Cancel" o ha chiuso la finestra modale,
	                // gestisci di conseguenza
	            }
			}, ModalityState.NON_MODAL);
			// Non è più necessario attendere che il processo termini, poiché è stato //
			// eseguito in un thread separato

		} catch (Exception ex) {
			ex.printStackTrace();
			Messages.showErrorDialog("Error during the execution of Python script", "Error during the execution");
		} finally {
			if (scriptFile.exists()) {
				scriptFile.delete();
			}
			this.setEnabled(true);
		}
	}


	private void executeExternalScript(String parameters) {
		File scriptFile = null;
		try {

			this.setEnabled(false);
			String tempDir = System.getProperty("java.io.tmpdir");
			scriptFile = new File(tempDir, "script_External_TDD.py");
			Files.copy(getClass().getResourceAsStream("/scripts/script_External_TDD.py"), scriptFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Project p = parent.getP();
			String command = "python " + scriptFile.getAbsolutePath() + " " + parameters + " -p "
					+'"'+parent.getP().getBasePath() + File.separator+"*"+'"';
			Process process = Runtime.getRuntime().exec(command);
			// Leggi l'output dello script Python
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			StringBuilder errorOutput = new StringBuilder();


			StringBuilder output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}

			String lineError;
			while ((lineError = readerError.readLine()) != null) {
				errorOutput.append(lineError).append("\n");
			}

			readerError.close();

			JFrame mainFrame = WindowManager.getInstance().getFrame(parent.getP());
			mainFrame.setCursor(Cursor.getDefaultCursor());

			ApplicationManager.getApplication().invokeLater(()->{
				//Old View Implementation
				/*CodeDialog dialog = new CodeDialog(parent.getP(), "\n" + output);
	            dialog.show();*/
				//New View

				String content = "ERROR";
				try {
					content = new String(Files.readAllBytes(Paths.get(parent.getPath())));
				} catch (IOException e) {
					e.printStackTrace();
				}

				CodeDiffDialog dialog = new CodeDiffDialog(parent.getP(),content,output.toString());
				DocumentContent contentModified = dialog.getContent();

				dialog.show();

				if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
					String path = this.getParent().getPath();

					sovrascriviFile(parent.getP(), path, contentModified.getDocument().getText());
					refreshFile(parent.getP(), path);
				} else {
					// L'utente ha cliccato su "Cancel" o ha chiuso la finestra modale,
					// gestisci di conseguenza
				}
			}, ModalityState.NON_MODAL);
			// Non è più necessario attendere che il processo termini, poiché è stato //
			// eseguito in un thread separato

		} catch (Exception ex) {
			ex.printStackTrace();
			Messages.showErrorDialog("Error during the execution of Python script", "Error during the execution");
		} finally {
			if (scriptFile.exists()) {
				scriptFile.delete();
			}
			this.setEnabled(true);
		}
	}

	public static void refreshFile(Project project, String absolutePath) {
		// Assicurati che il progetto e il percorso assoluto non siano nulli
		if (project == null || absolutePath == null) {
			return;
		}

		// Ottieni il sistema di file locale
		LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

		// Trova il VirtualFile associato al percorso assoluto
		VirtualFile virtualFile = localFileSystem.findFileByPath(absolutePath);

		// Se il VirtualFile esiste, esegui il refresh
		if (virtualFile != null) {
			WriteCommandAction.runWriteCommandAction(project, () -> {
				virtualFile.refresh(false, true);
			});
		}
	}

	private static void sovrascriviFile(Project p, String percorsoFile, String nuovoContenuto) {
		try {
			LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
			VirtualFile file = localFileSystem.findFileByPath(percorsoFile);

			WriteCommandAction.runWriteCommandAction(p, () -> {
				try {
					// Ottieni il documento del file e modificalo in modo sicuro per la scrittura
					Document document = FileDocumentManager.getInstance().getDocument(file);
					if (document != null) {
						document.setText(nuovoContenuto);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
