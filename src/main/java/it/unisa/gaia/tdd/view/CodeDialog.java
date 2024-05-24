package it.unisa.gaia.tdd.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CodeDialog extends DialogWrapper {
    private String code;

    public CodeDialog(@Nullable Project project, String code) {
        super(project, true);
        this.code = code;
        init();
        setTitle("Output");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea textArea = new JTextArea(code);
        textArea.setEditable(false);

        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        return panel;
    }

    @Override
    protected JComponent createSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton improveButton = new JButton("Improve");
        improveButton.addActionListener(e -> onImprove());
        panel.add(improveButton);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> close(OK_EXIT_CODE));
        panel.add(okButton);
        return panel;
    }

    private void onImprove() {
        // Handle improve action
        close(OK_EXIT_CODE);
    }


    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();
        myOKAction.putValue(Action.NAME, "OK");
    }

    @Override
    protected void doOKAction() {
        // Aggiungi qui la logica per l'azione "OK" se necessario
        super.doOKAction();
    }

    @Override
	public void doCancelAction() {
        // Aggiungi qui la logica per l'azione "Cancel" se necessario
        super.doCancelAction();
    }

    public static void showCodeDialog(Project project, String code) {
        CodeDialog dialog = new CodeDialog(project, code);
        dialog.show();
    }
}
