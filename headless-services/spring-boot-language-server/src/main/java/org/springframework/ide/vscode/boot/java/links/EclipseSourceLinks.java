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
package org.springframework.ide.vscode.boot.java.links;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;
import org.springframework.ide.vscode.commons.java.IType;

/**
 * Source links for Eclipse client. Eclipse IntroURLs.
 *
 * @author Alex Boyko
 *
 */
public class EclipseSourceLinks implements SourceLinks {

	private static final String URL_PREFIX = "http://org.eclipse.ui.intro/execute?command=";
	private static final String EQUALS = "=";
	private static final String PARAMETERS_SEPARATOR = ",";
	private static final String PARAMETERS_START = "(";
	private static final String PARAMETERS_END = ")";

	private static final String JAVA_ELEMENT_COMMAND = "org.springframework.tooling.boot.ls.OpenJavaElement";
	private static final String BINDING_KEY_PARAMETER_ID = "bindingKey";
	private static final String PROJECT_NAME_PARAMETER_ID = "projectName";

	private static final String RESOURCE_COMMAND = "org.springframework.tooling.boot.ls.OpenResourceInEditor";
	private static final String PATH = "path";

	private static final Logger log = LoggerFactory.getLogger(EclipseSourceLinks.class);

	@Override
	public Optional<String> sourceLinkUrlForFQName(IJavaProject project, String fqName) {
		return Optional.ofNullable(eclipseIntroUri(project, fqName)).map(uri -> uri.toString());
	}

	@Override
	public Optional<String> sourceLinkUrlForClasspathResource(IJavaProject project, String path) {
		int idx = path.lastIndexOf(CLASS);
		if (idx >= 0) {
			Path p = Paths.get(path.substring(0, idx));
			return sourceLinkUrlForFQName(project, p.toString().replace(File.separator, "."));
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> sourceLinkForResourcePath(Path path) {
		return Optional.ofNullable(eclipseIntroUri(path)).map(uri -> uri.toString());
	}

	public static URI eclipseIntroUri(Path path) {
		if (path != null) {
			try {
				StringBuilder paramBuilder = new StringBuilder(RESOURCE_COMMAND);
				paramBuilder.append(PARAMETERS_START);
				paramBuilder.append(PATH);
				paramBuilder.append(EQUALS);
				paramBuilder.append(path);
				paramBuilder.append(PARAMETERS_END);

				StringBuilder urlBuilder = new StringBuilder(URL_PREFIX);
				urlBuilder.append(URLEncoder.encode(paramBuilder.toString(), "UTF8"));
				return URI.create(urlBuilder.toString());
			} catch (UnsupportedEncodingException e) {
				log.error("{}", e);
			}
		}
		return null;
	}

	public static URI eclipseIntroUri(IJavaProject project, String fqName) {
		IType type = project.findType(fqName);
		return type == null ? null : eclipseIntroUri(project, type);
	}

	public static URI eclipseIntroUri(IJavaProject project, IMember member) {
		try {
			StringBuilder paramBuilder = new StringBuilder(JAVA_ELEMENT_COMMAND);
			paramBuilder.append(PARAMETERS_START);
			paramBuilder.append(BINDING_KEY_PARAMETER_ID);
			paramBuilder.append(EQUALS);
			paramBuilder.append(member.getBindingKey());
			if (project != null && project.getElementName() != null) {
				paramBuilder.append(PARAMETERS_SEPARATOR);
				paramBuilder.append(PROJECT_NAME_PARAMETER_ID);
				paramBuilder.append(EQUALS);
				paramBuilder.append(project.getElementName());
			}
			paramBuilder.append(PARAMETERS_END);

			StringBuilder urlBuilder = new StringBuilder(URL_PREFIX);
			urlBuilder.append(URLEncoder.encode(paramBuilder.toString(), "UTF8"));
			return URI.create(urlBuilder.toString());
		} catch (UnsupportedEncodingException e) {
			log.error("{}", e);
		}
		return null;
	}

}
