/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - emilie.balland@inria.fr (INRIA)
*******************************************************************************/
package org.rascalmpl.eclipse.nature;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;


public class Nature implements IProjectNature {
	private IProject project;

	public static String getNatureId() {
		return "rascal.nature";
	}
	
	public void configure() throws CoreException {
		IFolder folder = project.getFolder(IRascalResources.RASCAL_SRC);
		
		if (!folder.exists()) {
			folder.create(false, false, null);
		}
		
		link();
	}

	public void deconfigure() throws CoreException {

	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	private void link() throws CoreException {
		try {
			IFolder lib = project.getFolder("std");
			
			if (!lib.exists()) {
				lib.createLink(new URI("rascal-library", RascalLibraryFileSystem.RASCAL, "", null), 0, null);
			}

			lib = project.getFolder("eclipse");
			
			if (!lib.exists()) {
				lib.createLink(new URI("rascal-library", RascalLibraryFileSystem.ECLIPSE, "", null), 0, null);
			}
		} catch (URISyntaxException e) {
			Activator.getInstance().logException("error during linking of libraries", e);
		}
	}
}
