/*
 *  Copyright (c) 2012-2013, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package se.jbee.inject.service;

public class ServiceMalfunction extends RuntimeException {

	public ServiceMalfunction(String message, Throwable cause) {
		super(message, cause);
	}
	
}