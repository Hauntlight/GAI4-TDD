package it.unisa.gaia.tdd.view;

import com.intellij.ide.presentation.Presentation;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI.CurrentTheme.ActionButton;

import it.unisa.gaia.tdd.GAIA;
import it.unisa.gaia.tdd.control.AzioneAssistente;
import it.unisa.gaia.tdd.control.AzioneAssistenteSmart;
import it.unisa.gaia.tdd.control.AzioneRefactor;
import it.unisa.gaia.tdd.control.AzioneVerificaCoverage;
import it.unisa.gaia.tdd.gai4settings;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GPTAssistantToolWindowPanel extends JPanel {
    JTextField classeDaCompletareTextField;
    private JTextField testClassTextField;

    private JButton buttonCoverage;
    private Project p;
    
    public Project getP() {
    	return p;
    }
    
    public String getPath() {
    	return classeDaCompletareTextField.getText();
    }
    
    public void setP(Project p) {
		this.p = p;
	}




	public String getParameters() {
    	//TODO Gestione eccezioni
    	return "-c "+'"'+classeDaCompletareTextField.getText()+'"'+" -tc "+'"'+testClassTextField.getText()+'"';
    }

    public GPTAssistantToolWindowPanel(String filePath, Project project) {
        // Inizializza il pannello con il percorso del file
        initPanel(filePath, project);
    }

    private void initPanel(String filePath, Project project) {
        setLayout(null);
        setPreferredSize (new Dimension (944, 563));
        // TextField per la "Classe da Completare"
        classeDaCompletareTextField = new JTextField(filePath);
        JLabel lbl = new JLabel("Class under test");
        add(lbl);
        add(classeDaCompletareTextField);

        // TextField per la "Test Class"
        testClassTextField = new JTextField();
        JLabel lbl2 = new JLabel("Test Class:");
        add(lbl2);
        add(testClassTextField);
        JButton button = new JButton(new AzioneAssistente(this));
        add(button);
        JButton buttonRefactor = new JButton(new AzioneRefactor(this));
        add(buttonRefactor);

        JButton improvedButton = new JButton(new AzioneAssistenteSmart(this));
        add(improvedButton);

        buttonCoverage = new JButton(new AzioneVerificaCoverage(this));
        add(buttonCoverage);
        
        classeDaCompletareTextField.setBounds (5, 25, 925, 25);
        testClassTextField.setBounds (5, 75, 925, 25);
        lbl.setBounds (5, 0, 500, 25);
        lbl2.setBounds (5, 50, 500, 25);
        button.setBounds (5, 120, 150, 25);
        buttonCoverage.setBounds (185, 120, 150, 25);
        buttonRefactor.setBounds(365, 120, 150, 25);
        improvedButton.setBounds(565, 120, 180, 25);
    }
    public void updateContent(String filePath, Project project) {
        this.classeDaCompletareTextField.setText(filePath);
        String model = gai4settings.getInstance().getAssistantModel();
        if (model.contains("gpt") && this.buttonCoverage != null){
            this.buttonCoverage.setEnabled(true);
        }
    }
}
