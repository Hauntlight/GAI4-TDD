package it.unisa.gaia.tdd;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;

import it.unisa.gaia.tdd.gai4settings.State;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;

import javax.swing.*;


//MainClass
//Gestisce la finestra delle impostazioni del tool
public class GAIA implements Configurable {
    private JPanel panel;
    private JTextField dataTextField;
    private JComboBox<String> comboModel;
    private JTextField dataTextFieldEx;
    private JTextField dataTextFieldExKey;

    private String[] comboModelItems = {"gpt-4-turbo","gpt-4o","gpt-4", "External"};

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "GAI4-TDD";
    }

    @Override
    public @Nullable JComponent createComponent() {
        // Crea il pannello delle preferenze UI
        panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setLayout(null);
        panel.setPreferredSize (new Dimension(833, 91));
        JLabel label = new JLabel("GPT API Key");
        dataTextField = new JTextField(64);
        JLabel labelExKey = new JLabel("External Server Key");
        dataTextFieldExKey = new JTextField(64);
        JLabel labelEx = new JLabel("External server <ip>:<port>");
        dataTextFieldEx = new JTextField(64);
        comboModel = new JComboBox<String> (comboModelItems);
        JLabel labelModel = new JLabel ("Model");
        panel.add(comboModel);
        panel.add(labelModel);
        panel.add(label);
        panel.add(dataTextField);
        panel.add(labelEx);
        panel.add(dataTextFieldEx);
        panel.add(labelExKey);
        panel.add(dataTextFieldExKey);
        comboModel.setBounds(10, 120, 260, 30);
        labelModel.setBounds(10, 80, 260, 25);
        dataTextField.setBounds (10, 40, 815, 35);
        label.setBounds (10, 10, 100, 25);
        labelEx.setBounds (10, 160, 200, 25);
        dataTextFieldEx.setBounds(10, 200, 200, 35);
        labelExKey.setBounds (10, 240, 200, 25);
        dataTextFieldExKey.setBounds (10, 280, 200, 35);
        return panel;
    }

    @Override
    public boolean isModified() {
        // Controlla se il valore del campo è stato modificato
        return (!dataTextField.getText().equals(getStoredData().apiKey) || !comboModel.getSelectedItem().equals(getStoredData().assistantModel) || !dataTextFieldEx.getText().equals(getStoredData().server) || !dataTextFieldExKey.getText().equals(getStoredData().serverKey));
    }

    @Override
    public void apply() throws ConfigurationException {
        // Salva il valore nelle preferenze
    	State state = new State();
    	state.apiKey = dataTextField.getText();
    	state.assistantModel = (String)(comboModel.getSelectedItem());
    	state.server = dataTextFieldEx.getText();
    	state.serverKey = dataTextFieldExKey.getText();
    	if(state.assistantModel == null) {
    		throw new ConfigurationException((String) comboModel.getSelectedItem());
    	}
        setStoredData(state);
    }

    @Override
    public void reset() {
        // Ripristina il valore dallo stato salvato
        dataTextField.setText(getStoredData().apiKey);
        dataTextFieldEx.setText(getStoredData().server);
        dataTextFieldExKey.setText(getStoredData().serverKey);
        if(getStoredData().assistantModel.equalsIgnoreCase("External")) {
            comboModel.setSelectedIndex(3);
        }else if(getStoredData().assistantModel.contains("4o")){
            comboModel.setSelectedIndex(1);
        }else if (getStoredData().assistantModel.contains("turbo")) {
            comboModel.setSelectedIndex(0);
        }else{
            comboModel.setSelectedIndex(2);
        }
    }

    @Override
    public void disposeUIResources() {
        // Libera le risorse
    }

    private State getStoredData() {
        // Recupera il valore memorizzato dalle preferenze di PyCharm
        return gai4settings.getInstance().getState();
    }

    private void setStoredData(State stato) {
        // Salva il valore nelle preferenze di PyCharm
    	gai4settings.getInstance().setApiKey(stato.apiKey);
    	gai4settings.getInstance().setAssistantModel(stato.assistantModel);
    	gai4settings.getInstance().setServer(stato.server);
    	gai4settings.getInstance().setServerKey(stato.serverKey);
    }
}
