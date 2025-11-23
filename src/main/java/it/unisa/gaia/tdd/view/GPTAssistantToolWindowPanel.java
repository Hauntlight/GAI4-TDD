package it.unisa.gaia.tdd.view;

import com.intellij.openapi.project.Project;
import it.unisa.gaia.tdd.control.AzioneAssistente;
import it.unisa.gaia.tdd.control.AzioneAssistenteSmart;
import it.unisa.gaia.tdd.control.AzioneVerificaCoverage;
import it.unisa.gaia.tdd.gai4settings;

import javax.swing.*;
import java.awt.*;

public class GPTAssistantToolWindowPanel extends JPanel {
    JTextField classeDaCompletareTextField;
    private JTextField testClassTextField;
    private JButton buttonCoverage;
    private Project p;

    public Project getP() { return p; }
    public void setP(Project p) { this.p = p; }

    public String getPath() { return classeDaCompletareTextField.getText(); }
    public String getTestPath() { return testClassTextField.getText(); }

    public GPTAssistantToolWindowPanel(String filePath, Project project) {
        initPanel(filePath, project);
    }

    private void initPanel(String filePath, Project project) {
        setLayout(null);
        setPreferredSize(new Dimension(944, 563));

        classeDaCompletareTextField = new JTextField(filePath);
        JLabel lbl = new JLabel("Class under test");
        add(lbl);
        add(classeDaCompletareTextField);

        testClassTextField = new JTextField();
        JLabel lbl2 = new JLabel("Test Class:");
        add(lbl2);
        add(testClassTextField);

        JButton button = new JButton(new AzioneAssistente(this));
        add(button);

        // Refactor and Smart buttons
        JButton improvedButton = new JButton(new AzioneAssistenteSmart(this));
        //add(improvedButton);

        // Coverage button
        buttonCoverage = new JButton(new AzioneVerificaCoverage(this));
        //add(buttonCoverage);

        classeDaCompletareTextField.setBounds(5, 25, 925, 35);
        testClassTextField.setBounds(5, 75, 925, 35);
        lbl.setBounds(5, 0, 500, 35);
        lbl2.setBounds(5, 50, 500, 35);
        button.setBounds(5, 120, 150, 35);
        buttonCoverage.setBounds(185, 120, 150, 35);
        improvedButton.setBounds(365, 120, 180, 35);
    }

    public void updateContent(String filePath, Project project) {
        this.classeDaCompletareTextField.setText(filePath);
        String model = gai4settings.getInstance().getAssistantModel();
        if (model != null && model.contains("gpt") && this.buttonCoverage != null) {
            this.buttonCoverage.setEnabled(true);
        }
    }
}