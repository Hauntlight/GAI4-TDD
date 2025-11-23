package it.unisa.gaia.tdd.model;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.project.Project;
import it.unisa.gaia.tdd.view.MyDiffWindow; // Imported correctly
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

// Removed 'extends DiffManagerEx' to avoid abstract method boilerplate.
// The CodeDiffDialog uses this class directly, so inheritance isn't required for this logic.
public class MyDiffManager {
    private MyDiffWindow window;

    public JComponent getPanel(@Nullable Project project, @NotNull DiffRequest request, @NotNull DiffDialogHints hints) {
        DiffRequestChain requestChain = new SimpleDiffRequestChain(request);
        window = new MyDiffWindow(project, requestChain, hints);
        return window.getPanel();
    }

    public void update() {
        if (window != null) window.update();
    }
}