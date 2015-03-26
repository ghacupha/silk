/*
 *  Copyright (c) 2012-2013, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package se.jbee.inject.bind;

import static se.jbee.inject.container.Scoped.DEPENDENCY;
import static se.jbee.inject.container.Scoped.TARGET_INSTANCE;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import se.jbee.inject.bootstrap.Bootstrapper.ModularBootstrapper;
import se.jbee.inject.bootstrap.ModularBundle;
import se.jbee.inject.bootstrap.SuppliedBy;
import se.jbee.inject.container.Provider;

/**
 * Installs all the build-in functionality by using the core API.
 */
public enum BuildinBundle
		implements ModularBundle<BuildinBundle> {
	/**
	 * Adds: {@link Provider}s can be injected for all bound types.
	 */
	PROVIDER,
	/**
	 * Adds: {@link List}s can be injected for all bound types (via array bridge)
	 */
	LIST,
	/**
	 * Adds: {@link Set} can be injected for all bound types (via array bridge)
	 */
	SET,
	/**
	 * Adds: {@link Collection} can be injected instead of {@link List} (needs explicit List bind).
	 */
	COLLECTION,
	/**
	 * Adds: {@link Logger}s can be injected per receiving class.
	 */
	LOGGER;

	@Override
	public void bootstrap( ModularBootstrapper<BuildinBundle> bootstrap ) {
		bootstrap.install( ListBridgeModule.class, LIST );
		bootstrap.install( SetBridgeModule.class, SET );
		bootstrap.install( CollectionBridgeModule.class, COLLECTION );
		bootstrap.install( ProviderBridgeModule.class, PROVIDER );
		bootstrap.install( LoggerModule.class, LOGGER );
	}

	private static class LoggerModule
			extends BinderModule {

		@Override
		protected void declare() {
			per( TARGET_INSTANCE ).starbind( Logger.class ).to( SuppliedBy.LOGGER );
		}

	}

	private static class ProviderBridgeModule
			extends BinderModule {

		@Override
		protected void declare() {
			per( DEPENDENCY ).starbind( Provider.class ).to( SuppliedBy.PROVIDER_BRIDGE );
		}

	}

	private static class ListBridgeModule
			extends BinderModule {

		@Override
		protected void declare() {
			per( DEPENDENCY ).starbind( List.class ).to( SuppliedBy.LIST_BRIDGE );
		}

	}

	private static class SetBridgeModule
			extends BinderModule {

		@Override
		protected void declare() {
			per( DEPENDENCY ).starbind( Set.class ).to( SuppliedBy.SET_BRIDGE );
		}

	}

	private static class CollectionBridgeModule
			extends BinderModule {

		@Override
		protected void declare() {
			asDefault().per( DEPENDENCY ).starbind( Collection.class ).toParametrized( List.class );
		}
	}

}