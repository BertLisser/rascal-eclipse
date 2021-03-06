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
package org.rascalmpl.eclipse.console.internal;

import org.rascalmpl.interpreter.Evaluator;


/**
 * Interpreters should implement this.
 * 
 * @author Arnold Lankamp
 */
public interface IInterpreter{
	
	/**
	 * Initializes the console.
	 */
	void initialize(Evaluator eval);
	
	/**
	 * Requests the interpreter to execute the given command.
	 * 
	 * @param command
	 *          The command to execute.
	 * @return True if the command was completed; false if it wasn't.
	 * @throws CommandExecutionException
	 *          Thrown when an exception occurs during the processing of a command. The message
	 *          contained in the exception will be printed in the console.
	 * @throws TerminationException
	 *          Thrown when the executed command triggers a termination request for the console.
	 */
	boolean execute(String command) throws CommandExecutionException, TerminationException;
	
	/**
	 * Associated the given console this this interpreter. This method is called during the
	 * initialization of the console.
	 * 
	 * @param console
	 *          The console to associate with this interpreter.
	 */
	void setConsole(IInterpreterConsole console);
	
	/**
	 * Returns the output that was generated by the last executed command.
	 * 
	 * @return The output that was generated by the last executed command.
	 */
	String getOutput();
	
	/**
	 * Requests the interpreter to terminate. This method is called by the console; users are
	 * discouraged from calling this method, instead IInterpreterConsole#terminate() should be
	 * used.
	 */
	void terminate();
	
	/**
	 * Requests the interpreter to stop what it is doing now and return to an initial state.
	 */
	void interrupt();
	
	/**
	 * Gives the interpreter the command to persist the given history.
	 * 
	 * @param history
	 *          The command history associated with the console.
	 */
	void storeHistory(CommandHistory history);

	/**
	 * Prints the current stack trace
	 */
	String getTrace();
}
