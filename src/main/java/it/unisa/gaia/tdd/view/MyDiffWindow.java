package it.unisa.gaia.tdd.view;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.impl.DiffWindowBase;
import com.intellij.diff.util.DiffUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyDiffWindow extends DiffWindowBase {
    @NotNull
    private final DiffRequestChain myRequestChain;

    public MyDiffWindow(@Nullable Project project, @NotNull DiffRequestChain requestChain,
                        @NotNull DiffDialogHints hints) {
        super(project, hints);
        myRequestChain = requestChain;
    }

    public JComponent getPanel() {
        this.init();
        return this.getWrapper().getComponent();
    }

    public void update() {
        this.getProcessor().updateRequest();
    }

    @NotNull
    @Override
    protected DiffRequestProcessor createProcessor() {
        return new MyCacheDiffRequestChainProcessor(myProject, myRequestChain);
    }

    private class MyCacheDiffRequestChainProcessor extends CacheDiffRequestChainProcessor {
        MyCacheDiffRequestChainProcessor(@Nullable Project project, @NotNull DiffRequestChain requestChain) {
            super(project, requestChain);
        }

        @Override
        protected void setWindowTitle(@NotNull String title) {
            getWrapper().setTitle(title);
        }

        //@Override
        protected void onAfterNavigate() {
            DiffUtil.closeWindow(getWrapper().getWindow(), true, true);
        }
    }
}