/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class Utils {

	public static Stream<ITextViewer> getActiveTextViewers() {
		return getActiveEditors()
			.map(editorPart -> editorPart.getAdapter(ITextViewer.class))
			.filter(Objects::nonNull);
	}

	public static Stream<IEditorPart> getActiveEditors() {
		return Arrays.stream(PlatformUI.getWorkbench().getWorkbenchWindows())
				.filter(Objects::nonNull)
				.flatMap(ww -> Arrays.stream(ww.getPages()))
				.filter(Objects::nonNull)
				.flatMap(page -> Arrays.stream(page.getEditorReferences()))
				.filter(Objects::nonNull)
				.map(ref -> ref.getEditor(false))
				.filter(Objects::nonNull);
	}

	public static Stream<ISourceViewer> getActiveSourceViewers() {
		return getActiveTextViewers()
				.filter(viewer -> viewer instanceof ISourceViewer)
				.map(viewer -> (ISourceViewer) viewer);
	}

	public static URI findDocUri(IDocument doc) {
		for (LSPDocumentInfo info : LanguageServiceAccessor.getLSPDocumentInfosFor(doc, (x) -> true)) {
			return info.getFileUri();
		}
		return null;
	}

	public static boolean isProperDocumentIdFor(IDocument doc, VersionedTextDocumentIdentifier id) {
		for (LSPDocumentInfo info : LanguageServiceAccessor.getLSPDocumentInfosFor(doc, (x) -> true)) {
			if (info.getVersion() == id.getVersion()) {
				URI uri = info.getFileUri();
				if (uri != null && uri.toString().equals(id.getUri())) {
					return true;
				}
			}
		}
		return false;
	}


	public static IJavaElement findElement(IJavaProject project, String bindingKey) {
		IJavaElement element = null;
		try {
			element = project.findElement(bindingKey, null);
		} catch (Throwable t) {
			// ignore
		}
		if (element == null) {
			// Try modifying the binding key to search for the alternate binding
			try {
				String alternateBinding = alternateBinding(bindingKey);
				if (alternateBinding != null) {
					element = project.findElement(alternateBinding, null);
				}
			} catch (Throwable t) {
				// ignore
			}
		}
		return element;
	}

	private static String alternateBinding(String bindingKey) {
		int idxStartParams = bindingKey.indexOf('(');
		if (idxStartParams >= 0) {
			int idxEndParams = bindingKey.indexOf(')', idxStartParams);
			if (idxEndParams > idxStartParams) {
				String params = bindingKey.substring(idxStartParams, idxEndParams);
				return bindingKey.substring(0, idxStartParams) + params.replace('/', '.') + bindingKey.substring(idxEndParams);
			}
		}
		return null;
	}

}
