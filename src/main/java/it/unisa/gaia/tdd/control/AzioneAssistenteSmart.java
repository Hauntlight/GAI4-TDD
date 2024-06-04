package it.unisa.gaia.tdd.control;

import com.intellij.diff.contents.DocumentContent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import it.unisa.gaia.tdd.gai4settings;
import it.unisa.gaia.tdd.view.CodeDiffDialog;
import it.unisa.gaia.tdd.view.GPTAssistantToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;

public class AzioneAssistenteSmart extends AbstractAction {

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

	public AzioneAssistenteSmart(GPTAssistantToolWindowPanel parent) {
		super();
		this.parent = parent;
		this.putValue(NAME, "Optimized Green Phase");
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
			return;
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
		File coverageFile = null;
		File improvedScript = null;
		Path tempFile = null;
		int tentativi = 0;
		boolean coverage = false;
		try {

			this.setEnabled(false);
			String tempDir = System.getProperty("java.io.tmpdir");
			scriptFile = new File(tempDir, "script_GPT_TDD.py");
			coverageFile = new File(tempDir, "script_Coverage_TDD.py");
			improvedScript = new File(tempDir, "script_Improved_TDD.py");
			Files.copy(getClass().getResourceAsStream("/scripts/script_GPT_TDD.py"), scriptFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(getClass().getResourceAsStream("/scripts/script_coverage_GPT_TDD.py"), coverageFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(getClass().getResourceAsStream("/scripts/script_improved_GPT_TDD.py"), improvedScript.toPath(),
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


			StringBuilder result = new StringBuilder(parameters);

			while (true) {
				// Find the start of the "-c" marker
				int startIndex = result.indexOf("-c");
				// Find the start of the "-tc" marker
				int endIndex = result.indexOf("-tc");

				// If no more "-c" markers or they are out of order, break the loop
				if (startIndex == -1 || (endIndex != -1 && startIndex > endIndex)) {
					break;
				}

				// If "-tc" is not found after "-c", remove just "-c"
				if (endIndex == -1) {
					result.delete(startIndex, startIndex + 2); // length of "-c" is 2
					break;
				}

				//System.out.println(endIndex);

				// The end index should be the end of "-tc" marker
				//endIndex += 3; // 3 is the length of the marker "-tc"

				// Remove the substring between "-c" and "-tc"
				result.delete(startIndex, endIndex);
			}

			parameters = result.toString();

			tempFile = Files.createTempFile("tempFile", ".txt");
			String initialContent = output.toString();
			Files.write(tempFile, initialContent.getBytes(), StandardOpenOption.WRITE);
			//String initialOutput = output.toString();
			while (tentativi<3 && !coverage){

				

				command = "python " + coverageFile.getAbsolutePath() + " " + parameters + " -p "
						+'"'+parent.getP().getBasePath() + File.separator+"*"+'"' + " -c " + '"' + tempFile.toAbsolutePath().toString()+ '"';

				process = Runtime.getRuntime().exec(command);
				// Leggi l'output dello script Python
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				errorOutput = new StringBuilder();


				output = new StringBuilder();
				line = "";
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}

				lineError="";
				while ((lineError = readerError.readLine()) != null) {
					errorOutput.append(lineError).append("\n");
				}

				readerError.close();


				if (output.toString().replaceAll("\\s+", "").equalsIgnoreCase("TestsuiteOK")){

					coverage = true;

				}else{
					tentativi++;
					command = "python " + improvedScript.getAbsolutePath() + " " + parameters + " -p "
							+'"'+parent.getP().getBasePath() + File.separator+"*"+'"' + " -c " + '"' + tempFile.toAbsolutePath().toString() + '"';
					process = Runtime.getRuntime().exec(command);
					// Leggi l'output dello script Python
					reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					errorOutput = new StringBuilder();


					output = new StringBuilder();
					line = "";
					while ((line = reader.readLine()) != null) {
						output.append(line).append("\n");
					}

					lineError="";
					while ((lineError = readerError.readLine()) != null) {
						errorOutput.append(lineError).append("\n");
					}

					readerError.close();

					initialContent = output.toString();
					Files.write(tempFile, initialContent.getBytes(), StandardOpenOption.WRITE);
				}

			}

			final String improvedOutput = initialContent;


			final boolean coverageF = coverage;
			final int tentativiF = tentativi;

			JFrame mainFrame = WindowManager.getInstance().getFrame(parent.getP());
			mainFrame.setCursor(Cursor.getDefaultCursor());

			ApplicationManager.getApplication().invokeLater(()->{
				//Old View Implementation
				/*CodeDialog dialog = new CodeDialog(parent.getP(), "\n" + output);
	            dialog.show();*/
				//New View


				//Integer tentativiI = new Integer(tentativi);


				if(coverageF == true){// I NEED TO USE coverage here but i cant -> Variable used in lambda expression should be final or effectively final
					Messages.showMessageDialog(
							"Coverage achieved in : " + tentativiF +" attempts", // // I NEED TO USE tentativi here but i cant -> Variable used in lambda expression should be final or effectively final
							"Information",              // Title of the dialog
							Messages.getInformationIcon() // Icon to be displayed
					);
				}else{
					Messages.showMessageDialog(
							"Coverage not achieved in : " + tentativiF +" attempts", // I NEED TO USE tentativi here but i cant -> Variable used in lambda expression should be final or effectively final
							"Information",              // Title of the dialog
							Messages.getInformationIcon() // Icon to be displayed
					);
				}




				
				String content = "ERROR";
				try {
					 content = new String(Files.readAllBytes(Paths.get(parent.getPath())));
				} catch (IOException e) {
					e.printStackTrace();
				}

				CodeDiffDialog dialog = new CodeDiffDialog(parent.getP(),content,improvedOutput);
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
			if(improvedScript.exists()){
				improvedScript.delete();
			}
			if(coverageFile.exists()){
				coverageFile.delete();
			}
			if (tempFile != null) {
				tempFile.toFile().delete();
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
