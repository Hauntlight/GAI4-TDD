package it.unisa.gaia.tdd.view;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.WindowWrapper;
import it.unisa.gaia.tdd.model.MyDiffManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CodeDiffDialog extends DialogWrapper {
    private final Project project;
    private final String ogCode;
    private final String newCode;
    private MyDiffManager manager;
    private DocumentContent content;

    public CodeDiffDialog(Project project, String ogCode, String newCode) {
        super(true);
        setTitle("Code Diff Dialog");
        this.project = project;
        this.ogCode = ogCode;
        this.newCode = newCode;
        init();
    }

    public DocumentContent getContent() {
        return content;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        DocumentContent originalContent = DiffContentFactory.getInstance().create(ogCode);
        DocumentContent modifiedContent = DiffContentFactory.getInstance().createEditable(project, newCode, null);

        this.content = modifiedContent;

        SimpleDiffRequest diffRequest = new SimpleDiffRequest("Code Diff", originalContent, modifiedContent, "Original", "Modified");

        manager = new MyDiffManager();
        DiffDialogHints hints = new DiffDialogHints(WindowWrapper.Mode.MODAL, this.getWindow());

        return manager.getPanel(project, diffRequest, hints);
    }

    @Override
    public void show() {
        if (manager != null) manager.update();
        super.show();
    }
}