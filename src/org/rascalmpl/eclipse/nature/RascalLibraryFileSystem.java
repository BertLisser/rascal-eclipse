/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.nature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.Evaluator;

// TODO: link this stuff with the rascal search path instead
public class RascalLibraryFileSystem extends FileSystem {
	public static final String SCHEME = "rascal-library";
	public static final String ECLIPSE = "eclipse";
	public static final String RASCAL = "rascal";
	protected Map<String, RascalLibraryFileStore> roots = new HashMap<String, RascalLibraryFileStore>();

	public RascalLibraryFileSystem() {
		addRoot(RASCAL, Evaluator.class, "/org/rascalmpl/library");
		addRoot(ECLIPSE, Activator.class, "/org/rascalmpl/eclipse/library");
	}

	@Override
	public IFileStore getStore(URI uri) {
		if (!uri.getScheme().equals(SCHEME)) {
			return null;
		}
		
		return roots.get(uri.getHost());
	}
	
	private void addRoot(String name, Class<?> root, String loc) {
		try {
			URL resource = root.getResource(loc);

			if (resource == null) {
				Activator.getInstance().logException("can not find " + loc + " using " + root.getName(), new NullPointerException());
				return;
			}
			
			URL fileURL = FileLocator.toFileURL(resource);
		    // this constructor will not parse or escape spaces (workaround for Eclipse bug)
			URI fileURI = new URI(fileURL.getProtocol(), fileURL.getHost(), fileURL.getPath(), fileURL.getQuery());
			roots.put(name, new RascalLibraryFileStore(fileURI));
		} catch (URISyntaxException e) {
			Activator.getInstance().logException("linking library failed", e);
		} catch (IOException e) {
			Activator.getInstance().logException("linking library failed", e);
		}
	}

	public class RascalLibraryFileStore extends FileStore {
			private RascalLibraryFileStore parent;
			private File file;
	
			public RascalLibraryFileStore(File file, RascalLibraryFileStore parent) {
				this.parent = parent;
				this.file = file;
			}
			
			public RascalLibraryFileStore(URI root) {
				this.parent = null;
				this.file = new File(root.getPath());
			}
			
			@Override
			public boolean equals(Object obj) {
				if(obj == null)
					return false;
				if (obj.getClass() == getClass()) {
					RascalLibraryFileStore other = (RascalLibraryFileStore) obj;
					return file.equals(other.file);
				}
				return false;
			}
			
			@Override
			public int hashCode() {
				return file.hashCode();
			}
			
			
	
			@Override
			public String[] childNames(int options, IProgressMonitor monitor)
					throws CoreException {
				if (file.isDirectory()) {
					List<String> list = new LinkedList<String>();
					
					for (File f : file.listFiles()) {
						if (f.isDirectory() || f.getName().endsWith(".rsc")) {
							list.add(f.getName());
						}
					}
					
					return list.toArray(new String[list.size()]);
				}
				
				return EMPTY_STRING_ARRAY;
			}
	
			@Override
			public IFileInfo fetchInfo(int options, IProgressMonitor monitor)
					throws CoreException {
				FileInfo info = new FileInfo(getName());
				if (file.isDirectory()) {
					info.setDirectory(true);
					info.setLastModified(file.lastModified());
				} else {
					info.setDirectory(false);
					info.setLastModified(file.lastModified());
				}
				info.setExists(true);
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				return info;
			}
			
			@Override
			public IFileStore getChild(String name) {
				if (file.isDirectory()) {
					return new RascalLibraryFileStore(new File(file, name), this);
				}
				
				return null;
			}
	
			@Override
			public String getName() {
				return file.getName();
			}
	
			@Override
			public IFileStore getParent() {
				return parent;
			}
	
			@Override
			public InputStream openInputStream(int options, IProgressMonitor monitor)
					throws CoreException {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException e) {
					throw new CoreException(new Status(IStatus.ERROR, "", e.getMessage()));
				}
			}
	
			@Override
			public URI toURI() {
				return file.toURI();
			}
	
		}

}
