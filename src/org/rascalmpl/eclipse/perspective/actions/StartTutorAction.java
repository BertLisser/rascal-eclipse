package org.rascalmpl.eclipse.perspective.actions;

import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.library.experiments.RascalTutor.RascalTutor;


public class StartTutorAction implements IWorkbenchWindowActionDelegate {
	private static RascalTutor tutor;

	public static void stopTutor() {
		stop();
	}
	
	public void dispose() {
		stop();
	}
	
	private static void stop() {
		if (tutor != null) {
			try {
				tutor.stop();
				tutor = null;
			} catch (Exception e) {
				Activator.getInstance().logException("could not stop tutor", e);
			}
		}
	}

	public void init(IWorkbenchWindow window) {
		// do nothing
	}

	public void run(IAction action) {
		int port = 9000;
		try {
			if (tutor == null) {
				tutor = new RascalTutor();
				
				for (int i = 0; i < 100; i++) {
					try {
						tutor.start(port);
						break;
					}
					catch (BindException e) {
						port += 1;
					}
				}
			}
			
			int style = IWorkbenchBrowserSupport.AS_EDITOR 
					  | IWorkbenchBrowserSupport.LOCATION_BAR 
			          | IWorkbenchBrowserSupport.STATUS
			          ;
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(style, "RascalTutorBrowser", "RascalTutorBrowser", "Rascal Tutor");
			browser.openURL(new URL("http://localhost:" + port));
		} catch (PartInitException e) {
			Activator.getInstance().logException("Could not start browser for tutor", e);
		} catch (MalformedURLException e) {
			Activator.getInstance().logException("Could not start browser for tutor", e);
		}
		catch (Exception e) {
			Activator.getInstance().logException("Could not start tutor server", e);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}
}
