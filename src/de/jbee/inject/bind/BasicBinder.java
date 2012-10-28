/*
 *  Copyright (c) 2012, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package de.jbee.inject.bind;

import de.jbee.inject.Instance;
import de.jbee.inject.Packages;
import de.jbee.inject.Scope;
import de.jbee.inject.Supplier;

public interface BasicBinder {

	<T> TypedBasicBinder<T> bind( Instance<T> instance );

	interface TypedBasicBinder<T> {

		void to( Supplier<? extends T> supplier );

	}

	/**
	 * The ROOT- {@link RootBasicBinder}.
	 * 
	 * @author Jan Bernitt (jan.bernitt@gmx.de)
	 */
	public interface RootBasicBinder
			extends ScopedBasicBinder {

		ScopedBasicBinder per( Scope scope );

	}

	/**
	 * A {@link Scope} had been defined.
	 * 
	 * @author Jan Bernitt (jan.bernitt@gmx.de)
	 */
	interface ScopedBasicBinder
			extends TargetedBasicBinder {

		/**
		 * Means when the target type/instance is created and dependencies are injected into it.
		 */
		TargetedBasicBinder injectingInto( Instance<?> target );
	}

	/**
	 * Bindings have been restricted and are just effective within a special scope defined before
	 * using {@link ScopedBasicBinder#injectingInto(Instance)}-clauses.
	 * 
	 * @author Jan Bernitt (jan.bernitt@gmx.de)
	 */
	interface TargetedBasicBinder
			extends BasicBinder /* LocalisedBinder */{

		BasicBinder in( Packages packages );

	}

	interface LocalisedBasicBinder
			extends BasicBinder {

		LocalisedBasicBinder havingParent( Class<?> type );

		LocalisedBasicBinder havingDirectParent( Class<?> type );

	}

}
