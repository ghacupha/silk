/*
 *  Copyright (c) 2012, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package de.jbee.inject.bind;

/**
 * The basic idea is to split the binding process into 2 steps: installing modules and do bindings
 * in the installed modules.
 * 
 * Thereby it is possible to keep track of the modules that should be installed before actually
 * install them. This has two major benefits:
 * 
 * 1. it is possible and intentional to declare installation of the same module as often as wanted
 * or needed without actually installing them more then once. This allows to see other modules as
 * needed dependencies or 'parent'-modules.
 * 
 * 2. the installation can be the first step of the verification (in a unit-test). The binding can
 * be omitted so that overall test of a configuration can be very fast.
 * 
 * @author Jan Bernitt (jan.bernitt@gmx.de)
 * 
 */
public interface Bootstrapper {

	void install( Class<? extends Bundle> bundle );

	/**
	 * Uninstalling is very different from overriding. It allows to totally remove a well defined
	 * part from the consideration while a override complects the overridden and the overriding
	 * {@linkplain Bundle} with each other and requires to considerer both in a complected form.
	 * 
	 * To allow predictability uninstalling is a final decision. Further calls that install the very
	 * same {@link Bundle} will not re-install it!
	 * 
	 * There is no need to uninstall a {@link Module} since uninstalling the {@linkplain Bundle}
	 * that installed the module will effectively uninstall the module as well (as long as that
	 * module is not installed by another installed bundle too). Allowing to uninstall separate
	 * {@link Module}s would break the well defined borders of bundles and lead to the need to
	 * consider something close to overrides.
	 */
	void uninstall( Class<? extends Bundle> bundle );

	void install( Module module );

	<M extends Enum<M> & ModularBundle<M>> void install( M... modules );

	<M extends Enum<M> & ModularBundle<M>> void uninstall( M... modules );

	<C extends Enum<C> & Const> void install( Class<? extends ModularBundle<C>> bundle,
			Class<C> property );

	interface ModularBootstrapper<M> {

		/**
		 * Installs the bundle within the given module.
		 * 
		 * If the module passed hasn't been {@link Bootstrapper#install(Enum...)}ed the call will be
		 * ignored.
		 */
		void install( Class<? extends Bundle> bundle, M module );
	}

}
