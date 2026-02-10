package it.unisa.gaia.tdd;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class GAIA implements Configurable {
    private JPanel panel;
    private JTextField dataTextField;
    private JComboBox<String> comboModel;
    private JTextField dataTextFieldEx;
    private JTextField dataTextFieldExKey;

    // Added gpt-5.1-codex to the top
    private final String[] comboModelItems = {"gpt-5.1-codex", "gpt-5", "gpt-5.1", "gpt-4-turbo", "gpt-4o", "gpt-4", "claude-opus-4-6","External"};

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "GAI4-TDD";
    }

    @Override
    public @Nullable JComponent createComponent() {
        panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(833, 350));

        JLabel label = new JLabel("GPT API Key");
        dataTextField = new JTextField(64);
        JLabel labelModel = new JLabel("Model");
        comboModel = new JComboBox<>(comboModelItems);

        JLabel labelEx = new JLabel("External server <ip>:<port>");
        dataTextFieldEx = new JTextField(64);
        JLabel labelExKey = new JLabel("External Server Key");
        dataTextFieldExKey = new JTextField(64);

        panel.add(label);
        panel.add(dataTextField);
        panel.add(labelModel);
        panel.add(comboModel);
        panel.add(labelEx);
        panel.add(dataTextFieldEx);
        panel.add(labelExKey);
        panel.add(dataTextFieldExKey);

        // Layout positioning
        label.setBounds(10, 10, 100, 25);
        dataTextField.setBounds(10, 40, 815, 35);

        labelModel.setBounds(10, 80, 260, 25);
        comboModel.setBounds(10, 120, 260, 30);

        labelEx.setBounds(10, 160, 200, 25);
        dataTextFieldEx.setBounds(10, 200, 200, 35);

        labelExKey.setBounds(10, 240, 200, 25);
        dataTextFieldExKey.setBounds(10, 280, 200, 35);

        return panel;
    }

    @Override
    public boolean isModified() {
        gai4settings.State state = gai4settings.getInstance().getState();
        return !dataTextField.getText().equals(state.apiKey) ||
                !comboModel.getSelectedItem().equals(state.assistantModel) ||
                !dataTextFieldEx.getText().equals(state.server) ||
                !dataTextFieldExKey.getText().equals(state.serverKey);
    }

    @Override
    public void apply() throws ConfigurationException {
        gai4settings settings = gai4settings.getInstance();
        settings.setApiKey(dataTextField.getText());
        settings.setAssistantModel((String) comboModel.getSelectedItem());
        settings.setServer(dataTextFieldEx.getText());
        settings.setServerKey(dataTextFieldExKey.getText());
    }

    @Override
    public void reset() {
        gai4settings.State state = gai4settings.getInstance().getState();
        dataTextField.setText(state.apiKey);
        dataTextFieldEx.setText(state.server);
        dataTextFieldExKey.setText(state.serverKey);

        String model = state.assistantModel;
        if (model == null) model = "gpt-4";

        if ("External".equalsIgnoreCase(model)) {
            comboModel.setSelectedItem("External");
        } else {
            comboModel.setSelectedItem(model);
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
    }
}