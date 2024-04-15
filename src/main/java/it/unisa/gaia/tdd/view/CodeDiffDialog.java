package it.unisa.gaia.tdd.view;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffManagerEx;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileContent;
import com.intellij.diff.merge.MergeRequest;
import com.intellij.diff.merge.MergeTool;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.DiffTool;
import com.intellij.diff.tools.fragmented.UnifiedDiffTool;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diff.DiffViewer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.vfs.VirtualFile;

import it.unisa.gaia.tdd.model.MyDiffManager;

import org.jetbrains.annotations.CalledInAwt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Window;
import java.util.List;

import javax.swing.*;

public class CodeDiffDialog extends DialogWrapper {

    private Project project;
    private String ogCode;
    private String newCode;
    private MyDiffManager manager;
    private DiffRequest diffRequest;
    private VirtualFile file;
    private DocumentContent content;

    public CodeDiffDialog(Project project, String ogCode, String newCode) {
        super(true);
        setTitle("Code Diff Dialog");
        this.project = project;
        this.ogCode = ogCode;
        this.newCode = newCode;
        init();
    }
    
    public CodeDiffDialog(Project project, String ogCode, VirtualFile file) {
        super(true);
        setTitle("Code Diff Dialog");
        this.project = project;
        this.ogCode = ogCode;
        this.newCode = null;
        this.file = file;
        
        init();
    }
    
    public DocumentContent getContent() {
    	return content;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        // Create a panel containing the diff viewer
    	DiffContent originalContent;
    	DiffContent modifiedContent;
    	JComponent panel = new JPanel();
    	//if (this.newCode == null) {
    	//	originalContent = DiffContentFactory.getInstance().create(ogCode);
        //    modifiedContent = DiffContentFactory.getInstance().create(project,this.file);
    	//}else {
    	originalContent = DiffContentFactory.getInstance().create(ogCode);
        modifiedContent = DiffContentFactory.getInstance().createEditable(project,newCode,null);
    	//}
        // Create DiffContent from the strings
        content = (DocumentContent) modifiedContent;
    	

        // Create a simple unified diff request
        diffRequest = new SimpleDiffRequest("Code Diff", originalContent, modifiedContent, "Original", "Modified");

        manager = new MyDiffManager();
        DiffDialogHints hints = new DiffDialogHints(WindowWrapper.Mode.MODAL, this.getWindow());
        
        panel = manager.getPanel(project, diffRequest, hints);
        //DiffRequestPanel panel = DiffManager.getInstance().createRequestPanel(project, project, getWindow());
        // Create a custom DiffManager and show the diff in a dialog
        //CustomDiffManager.getInstance().showDiff(project, diffRequest, DiffDialogHints.NON_MODAL);

        // Return the panel containing the diff viewer
        return panel;
    }
    
    
    @Override
    public void show() {
    	//DiffDialogHints hints = new DiffDialogHints(WindowWrapper.Mode.MODAL, this.getWindow());
    	//manager.showDiff(project,diffRequest,hints);
    	manager.update();
    	super.show();
    }

    @Override
    protected Action[] createActions() {
        return super.createActions();
    }

    @Override
    protected void doOKAction() {
        
        super.doOKAction();
        // DO SOMETHING HERE AFTER OK BUTTON IS CLICKED
    }

   
}
