package org.meta_environment.rascal.eclipse.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

public class NewRascalFile extends Wizard implements INewWizard {
	private NewRascalFilePage page;
	private ISelection selection;
	private String moduleName;
	
	public NewRascalFile() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	public void addPages() {
		page = new NewRascalFilePage(selection);
		addPage(page);
	}
	
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String filename = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					int till = containerName.substring(1).indexOf("/");
					String containerToPutFileIn = containerName;
					String fileToCreate = filename;
					if(till != -1){
						containerToPutFileIn = containerName.substring(0, till + 1);
						fileToCreate = containerName.substring(till + 1) + "/" + filename;
					}
					
					fileToCreate = fileToCreate.startsWith("/src/") ? fileToCreate : "/src/" + fileToCreate;
					fileToCreate = fileToCreate.endsWith(".rsc") ? fileToCreate : fileToCreate + ".rsc";
					
					moduleName = fileToCreate;
					moduleName = moduleName.substring(moduleName.lastIndexOf('/') + 1, moduleName.length());
					moduleName = moduleName.substring(0, moduleName.length() - 4);
					
					System.err.println(containerToPutFileIn+" "+fileToCreate+" "+filename);
					
					doFinish(containerToPutFileIn, fileToCreate, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	private void doFinish(
		String containerName,
		String fileName,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	private InputStream openContentStream() {
		String contents = "module "+moduleName;
		return new ByteArrayInputStream(contents.getBytes());
	}
	
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "rascal_eclipse", IStatus.OK, message, null);
		throw new CoreException(status);
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}