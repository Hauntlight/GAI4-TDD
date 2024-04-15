package it.unisa.gaia.tdd.model;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import org.jetbrains.annotations.CalledInAwt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManagerEx;
import com.intellij.diff.DiffManagerImpl;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.DiffTool;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.impl.DiffRequestPanelImpl;
import com.intellij.diff.impl.DiffWindow;
import com.intellij.diff.merge.BinaryMergeTool;
import com.intellij.diff.merge.MergeRequest;
import com.intellij.diff.merge.MergeTool;
import com.intellij.diff.merge.MergeWindow;
import com.intellij.diff.merge.TextMergeTool;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.binary.BinaryDiffTool;
import com.intellij.diff.tools.dir.DirDiffTool;
import com.intellij.diff.tools.external.ExternalDiffTool;
import com.intellij.diff.tools.external.ExternalMergeTool;
import com.intellij.diff.tools.fragmented.UnifiedDiffTool;
import com.intellij.diff.tools.simple.SimpleDiffTool;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import it.unisa.gaia.tdd.view.MyDiffWindow;

public class MyDiffManager extends DiffManagerEx {
	MyDiffWindow window= null;

	@Override
	public void showDiff(@Nullable Project project, @NotNull DiffRequest request) {
		showDiff(project, request, DiffDialogHints.DEFAULT);
	}

	@Override
	public void showDiff(@Nullable Project project, @NotNull DiffRequest request, @NotNull DiffDialogHints hints) {
		DiffRequestChain requestChain = new SimpleDiffRequestChain(request);
		showDiff(project, requestChain, hints);
	}

	@Override
	public void showDiff(@Nullable Project project, @NotNull DiffRequestChain requests,
			@NotNull DiffDialogHints hints) {
		if (ExternalDiffTool.isDefault()) {
			ExternalDiffTool.show(project, requests, hints);
			return;
		}

		showDiffBuiltin(project, requests, hints);
	}

	@Override
	public void showDiffBuiltin(@Nullable Project project, @NotNull DiffRequest request) {
		showDiffBuiltin(project, request, DiffDialogHints.DEFAULT);
	}

	@Override
	public void showDiffBuiltin(@Nullable Project project, @NotNull DiffRequest request,
			@NotNull DiffDialogHints hints) {
		DiffRequestChain requestChain = new SimpleDiffRequestChain(request);
		showDiffBuiltin(project, requestChain, hints);
	}

	@Override
	public void showDiffBuiltin(@Nullable Project project, @NotNull DiffRequestChain requests,
			@NotNull DiffDialogHints hints) {
		new MyDiffWindow(project, requests, hints).show();
	}
	
	public JComponent getPanel(@Nullable Project project, @NotNull DiffRequest request, @NotNull DiffDialogHints hints) {
		DiffRequestChain requestChain = new SimpleDiffRequestChain(request);
		window = new MyDiffWindow(project, requestChain, hints);
		return window.getPanel();
	}

	@NotNull
	@Override
	public DiffRequestPanel createRequestPanel(@Nullable Project project, @NotNull Disposable parent,
			@Nullable Window window) {
		DiffRequestPanelImpl panel = new DiffRequestPanelImpl(project, window);
		Disposer.register(parent, panel);
		return panel;
	}
	
	public void update() {
		window.update();
	}

	@NotNull
	@Override
	public List<DiffTool> getDiffTools() {
		List<DiffTool> result = new ArrayList<>();
		Collections.addAll(result, DiffTool.EP_NAME.getExtensions());
		result.add(SimpleDiffTool.INSTANCE);
		result.add(UnifiedDiffTool.INSTANCE);
		result.add(BinaryDiffTool.INSTANCE);
		result.add(DirDiffTool.INSTANCE);
		return result;
	}

	@NotNull
	@Override
	public List<MergeTool> getMergeTools() {
		List<MergeTool> result = new ArrayList<>();
		Collections.addAll(result, MergeTool.EP_NAME.getExtensions());
		result.add(TextMergeTool.INSTANCE);
		result.add(BinaryMergeTool.INSTANCE);
		return result;
	}

	@Override
	@CalledInAwt
	public void showMerge(@Nullable Project project, @NotNull MergeRequest request) {
		if (ExternalMergeTool.isDefault()) {
			ExternalMergeTool.show(project, request);
			return;
		}

		showMergeBuiltin(project, request);
	}

	@Override
	@CalledInAwt
	public void showMergeBuiltin(@Nullable Project project, @NotNull MergeRequest request) {
		new MergeWindow(project, request).show();
	}
}
