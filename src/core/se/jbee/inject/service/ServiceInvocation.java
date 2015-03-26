/*
 *  Copyright (c) 2012-2013, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package se.jbee.inject.service;

import se.jbee.inject.Type;

/**
 * Frames the invocation of {@link ServiceMethod}s with further functionality that can be executed
 * {@link #before(Type, Object, Type)} or {@link #after(Type, Object, Type, Object, Object)} the invoked
 * {@linkplain ServiceMethod}. Thereby a state can be passed between these tow methods. The result
 * of first will be passed to the second as an argument. This allows them stay stateless.
 * 
 * A {@link ServiceInvocation} intentionally doesn't give any control or access over/to the invoked
 * {@linkplain ServiceMethod} in order to be able to grant the same actual function invocation even
 * with faulty {@linkplain ServiceInvocation}s in place. That includes catching all exceptions
 * thrown in {@link #before(Type, Object, Type)} or {@link #after(Type, Object, Type, Object, Object)}.
 * 
 * @author Jan Bernitt (jan@jbee.se)
 * 
 * @param <T>
 *            Type of the state transfered between {@link #before(Type, Object, Type)} and
 *            {@link #after(Type, Object, Type, Object, Object)}.
 */
public interface ServiceInvocation<T> {

	<P, R> T before( Type<P> parameter, P value, Type<R> returnType );

	<P, R> void after( Type<P> parameter, P value, Type<R> returnType, R result, T before );

	<P, R> void afterException( Type<P> parameter, P value, Type<R> returnType, Exception e, T before );

	/**
	 * Used to hook up a {@link ServiceInvocation} to {@link ServiceMethod}s.
	 * 
	 * When targeting is used for the extension it has to be chosen which of the 4 possible types
	 * the targeting is applied to.
	 * 
	 */
	enum ServiceInvocationExtension
			implements Extension<ServiceInvocationExtension, ServiceInvocation<?>> {

		/**
		 * The {@link Class} defined the method that becomes the injected {@link ServiceMethod}.
		 */
		DEFINING_TYPE,

		/**
		 * The {@link Class} (whose method) is receiving the injected {@link ServiceMethod}.
		 */
		INJECTED_TYPE,

		/**
		 * The {@link Type} of the {@link ServiceMethod}'s parameter generic.
		 */
		PARAMETER_TYPE,

		/**
		 * The {@link Type} of the {@link ServiceMethod}'s return type generic.
		 */
		RETURN_TYPE

	}

}
